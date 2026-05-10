package com.farm.platform.service;

import com.farm.platform.dto.*;
import com.farm.platform.entity.*;
import com.farm.platform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FarmTripService {

    private final FarmTripRepository tripRepo;
    private final FarmTripCategoryRepository categoryRepo;
    private final FarmTripBookingRepository bookingRepo;
    private final UserRepository userRepo;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final List<FarmTripStatus> BROWSABLE = List.of(
            FarmTripStatus.ACTIVE, FarmTripStatus.FULL, FarmTripStatus.CLOSED, FarmTripStatus.COMPLETED);

    /* ============================ 公開瀏覽 ============================ */

    public List<FarmTripCategoryResponse> listCategories() {
        return categoryRepo.findAllByOrderBySortOrderAsc().stream()
                .map(FarmTripCategoryResponse::from).toList();
    }

    public PageResponse<FarmTripResponse> listPublic(Long categoryId, TripType tripType, Pageable pageable) {
        FarmTripCategory cat = (categoryId != null) ? categoryRepo.findById(categoryId).orElse(null) : null;
        Page<FarmTrip> page;
        if (cat != null && tripType != null) {
            page = tripRepo.findByCategoryAndTripTypeAndStatusInOrderByTripStartAsc(cat, tripType, BROWSABLE, pageable);
        } else if (cat != null) {
            page = tripRepo.findByCategoryAndStatusInOrderByTripStartAsc(cat, BROWSABLE, pageable);
        } else if (tripType != null) {
            page = tripRepo.findByTripTypeAndStatusInOrderByTripStartAsc(tripType, BROWSABLE, pageable);
        } else {
            page = tripRepo.findByStatusInOrderByTripStartAsc(BROWSABLE, pageable);
        }
        return PageResponse.of(page, FarmTripResponse::from);
    }

    public FarmTripResponse getDetail(Long id) {
        FarmTrip t = tripRepo.findFullById(id)
                .orElseThrow(() -> new IllegalArgumentException("活動不存在"));
        return FarmTripResponse.from(t);
    }

    /* ============================ 小農:CRUD ============================ */

    @Transactional
    public FarmTripResponse create(String farmerEmail, FarmTripRequest req) {
        User farmer = getUser(farmerEmail);
        if (!farmer.hasRole(Role.FARMER)) throw new AccessDeniedException("非小農");

        FarmTripCategory cat = categoryRepo.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("活動類別不存在"));
        validateRequest(req);

        FarmTrip t = FarmTrip.builder()
                .farmer(farmer)
                .category(cat)
                .tripType(req.getTripType())
                .title(req.getTitle())
                .intro(req.getIntro())
                .imageUrl(req.getImageUrl())
                .location(req.getLocation())
                .price(req.getPrice())
                .capacity(req.getCapacity())
                .currentBookings(0)
                .tripStart(req.getTripStart())
                .tripEnd(req.getTripEnd())
                .bookStart(req.getBookStart())
                .bookEnd(req.getBookEnd())
                .status(FarmTripStatus.ACTIVE)
                .build();
        return FarmTripResponse.from(tripRepo.save(t));
    }

    @Transactional
    public FarmTripResponse update(String farmerEmail, Long id, FarmTripRequest req) {
        FarmTrip t = tripRepo.findFullById(id)
                .orElseThrow(() -> new IllegalArgumentException("活動不存在"));
        if (!t.getFarmer().getEmail().equals(farmerEmail)) throw new AccessDeniedException("無權編輯");

        FarmTripCategory cat = categoryRepo.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("活動類別不存在"));
        validateRequest(req);
        if (req.getCapacity() < t.getCurrentBookings()) {
            throw new IllegalArgumentException("名額不可小於已預約人數(" + t.getCurrentBookings() + ")");
        }

        t.setCategory(cat);
        t.setTripType(req.getTripType());
        t.setTitle(req.getTitle());
        t.setIntro(req.getIntro());
        t.setImageUrl(req.getImageUrl());
        t.setLocation(req.getLocation());
        t.setPrice(req.getPrice());
        t.setCapacity(req.getCapacity());
        t.setTripStart(req.getTripStart());
        t.setTripEnd(req.getTripEnd());
        t.setBookStart(req.getBookStart());
        t.setBookEnd(req.getBookEnd());
        // 名額調整後若還有空間,從 FULL 回到 ACTIVE
        if (t.getStatus() == FarmTripStatus.FULL && t.remainingCapacity() > 0) {
            t.setStatus(FarmTripStatus.ACTIVE);
        }
        return FarmTripResponse.from(t);
    }

    @Transactional
    public void cancel(String farmerEmail, Long id) {
        FarmTrip t = tripRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("活動不存在"));
        if (!t.getFarmer().getEmail().equals(farmerEmail)) throw new AccessDeniedException("無權取消");
        if (t.getStatus() == FarmTripStatus.COMPLETED) throw new IllegalStateException("已完成的活動無法取消");
        t.setStatus(FarmTripStatus.CANCELLED);
        // 連動取消所有 CONFIRMED 預約
        List<FarmTripBooking> confirmed = bookingRepo.findByFarmTripAndStatus(t, FarmTripBookingStatus.CONFIRMED);
        for (FarmTripBooking b : confirmed) {
            b.setStatus(FarmTripBookingStatus.CANCELLED);
            b.setCancelledAt(LocalDateTime.now());
        }
        t.setCurrentBookings(0);
    }

    public PageResponse<FarmTripResponse> listFarmerOwn(String farmerEmail, Pageable pageable) {
        User farmer = getUser(farmerEmail);
        if (!farmer.hasRole(Role.FARMER)) throw new AccessDeniedException("非小農");
        return PageResponse.of(tripRepo.findByFarmerOrderByCreatedAtDesc(farmer, pageable), FarmTripResponse::from);
    }

    /* ============================ 預約 ============================ */

    @Transactional
    public FarmTripBookingResponse book(String email, Long tripId, CreateFarmTripBookingRequest req) {
        User user = getUser(email);
        FarmTrip t = tripRepo.findFullById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("活動不存在"));

        LocalDateTime now = LocalDateTime.now();
        if (t.getStatus() == FarmTripStatus.CANCELLED) throw new IllegalStateException("此活動已取消");
        if (t.getStatus() == FarmTripStatus.COMPLETED) throw new IllegalStateException("此活動已結束");
        if (t.getStatus() == FarmTripStatus.CLOSED || now.isAfter(t.getBookEnd())) throw new IllegalStateException("已過報名期限");
        if (now.isBefore(t.getBookStart())) throw new IllegalStateException("尚未開始報名");
        if (t.getStatus() == FarmTripStatus.FULL || t.remainingCapacity() <= 0) throw new IllegalStateException("活動已額滿");
        if (req.getNumPeople() > t.remainingCapacity()) {
            throw new IllegalArgumentException("剩餘名額僅 " + t.remainingCapacity() + " 位");
        }
        if (t.getFarmer().getId().equals(user.getId())) {
            throw new IllegalStateException("無法預約自己的活動");
        }

        BigDecimal total = t.getPrice().multiply(BigDecimal.valueOf(req.getNumPeople()));

        FarmTripBooking b = FarmTripBooking.builder()
                .bookingNo(generateBookingNo())
                .farmTrip(t)
                .user(user)
                .numPeople(req.getNumPeople())
                .totalAmount(total)
                .contactName(req.getContactName())
                .contactPhone(req.getContactPhone())
                .note(req.getNote())
                .status(FarmTripBookingStatus.CONFIRMED)
                .build();
        bookingRepo.save(b);

        t.setCurrentBookings(t.getCurrentBookings() + req.getNumPeople());
        if (t.remainingCapacity() == 0) t.setStatus(FarmTripStatus.FULL);

        return FarmTripBookingResponse.from(b);
    }

    @Transactional
    public FarmTripBookingResponse cancelBooking(String email, Long bookingId) {
        FarmTripBooking b = bookingRepo.findFullById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("預約不存在"));
        if (!b.getUser().getEmail().equals(email)) throw new AccessDeniedException("無權取消此預約");
        if (b.getStatus() == FarmTripBookingStatus.CANCELLED) throw new IllegalStateException("已取消");
        if (b.getStatus() == FarmTripBookingStatus.COMPLETED) throw new IllegalStateException("已完成,無法取消");

        FarmTrip t = b.getFarmTrip();
        if (LocalDateTime.now().isAfter(t.getTripStart())) {
            throw new IllegalStateException("活動已開始,無法取消");
        }

        b.setStatus(FarmTripBookingStatus.CANCELLED);
        b.setCancelledAt(LocalDateTime.now());

        t.setCurrentBookings(Math.max(0, t.getCurrentBookings() - b.getNumPeople()));
        if (t.getStatus() == FarmTripStatus.FULL && t.remainingCapacity() > 0) {
            t.setStatus(FarmTripStatus.ACTIVE);
        }
        return FarmTripBookingResponse.from(b);
    }

    public PageResponse<FarmTripBookingResponse> myBookings(String email, Pageable pageable) {
        User me = getUser(email);
        return PageResponse.of(bookingRepo.findByUserOrderByBookedAtDesc(me, pageable), FarmTripBookingResponse::from);
    }

    public PageResponse<FarmTripBookingResponse> farmerBookings(String farmerEmail, FarmTripBookingStatus status, Pageable pageable) {
        User farmer = getUser(farmerEmail);
        if (!farmer.hasRole(Role.FARMER)) throw new AccessDeniedException("非小農");
        Page<FarmTripBooking> page = (status != null)
                ? bookingRepo.findByFarmTrip_FarmerAndStatusOrderByBookedAtDesc(farmer, status, pageable)
                : bookingRepo.findByFarmTrip_FarmerOrderByBookedAtDesc(farmer, pageable);
        return PageResponse.of(page, FarmTripBookingResponse::from);
    }

    /* ============================ 排程 ============================ */

    @Scheduled(fixedDelayString = "PT300S", initialDelayString = "PT30S")
    @Transactional
    public void refreshTripStatus() {
        LocalDateTime now = LocalDateTime.now();
        // 報名截止 -> CLOSED
        for (FarmTrip t : tripRepo.findByStatusInAndBookEndBefore(
                List.of(FarmTripStatus.ACTIVE, FarmTripStatus.FULL), now)) {
            t.setStatus(FarmTripStatus.CLOSED);
        }
        // 活動結束 -> COMPLETED + 預約完成
        for (FarmTrip t : tripRepo.findByStatusInAndTripEndBefore(
                List.of(FarmTripStatus.ACTIVE, FarmTripStatus.FULL, FarmTripStatus.CLOSED), now)) {
            t.setStatus(FarmTripStatus.COMPLETED);
            for (FarmTripBooking b : bookingRepo.findByFarmTripAndStatus(t, FarmTripBookingStatus.CONFIRMED)) {
                b.setStatus(FarmTripBookingStatus.COMPLETED);
                b.setCompletedAt(now);
            }
        }
    }

    /* ============================ helpers ============================ */

    private void validateRequest(FarmTripRequest r) {
        if (!r.getTripEnd().isAfter(r.getTripStart())) throw new IllegalArgumentException("活動結束必須晚於開始");
        if (!r.getBookEnd().isAfter(r.getBookStart())) throw new IllegalArgumentException("報名截止必須晚於開放報名");
        if (r.getBookEnd().isAfter(r.getTripStart())) throw new IllegalArgumentException("報名截止不可晚於活動開始");
    }

    private User getUser(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("使用者不存在"));
    }

    private String generateBookingNo() {
        String date = LocalDate.now().format(DATE_FMT);
        for (int i = 0; i < 5; i++) {
            String no = "FT-" + date + "-" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
            if (!bookingRepo.existsByBookingNo(no)) return no;
        }
        return "FT-" + date + "-" + System.currentTimeMillis() % 100000;
    }
}
