package com.farm.platform.service;

import com.farm.platform.dto.*;
import com.farm.platform.entity.*;
import com.farm.platform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final FarmTripSessionRepository sessionRepo;
    private final FarmTripAuditRepository auditRepo;
    private final FarmTripOrderRepository orderRepo;
    private final FarmTripCommentRepository commentRepo;
    private final MemberRepository memberRepo;
    private final FarmerRepository farmerRepo;
    private final AdminRepository adminRepo;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final List<FarmTripStatus> BROWSABLE = List.of(FarmTripStatus.ACTIVE);

    /* ============================ 公開瀏覽 ============================ */

    public PageResponse<FarmTripResponse> listPublic(TripType tripType, Pageable pageable) {
        Page<FarmTrip> page = (tripType != null)
                ? tripRepo.findByTripTypeAndStatusInOrderByCreatedAtDesc(tripType, BROWSABLE, pageable)
                : tripRepo.findByStatusInOrderByCreatedAtDesc(BROWSABLE, pageable);
        return PageResponse.of(page, FarmTripResponse::from);
    }

    public FarmTripResponse getDetail(Long id) {
        FarmTrip t = tripRepo.findFullById(id)
                .orElseThrow(() -> new IllegalArgumentException("活動不存在"));
        List<FarmTripSessionResponse> sessions = sessionRepo.findByFarmTripOrderByTripStartAsc(t).stream()
                .map(FarmTripSessionResponse::from).toList();
        return FarmTripResponse.from(t, sessions);
    }

    public List<FarmTripCommentResponse> listComments(Long tripId, int size) {
        FarmTrip t = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("活動不存在"));
        return commentRepo.findByFarmTripOrderByCreatedAtDesc(t, PageRequest.of(0, size))
                .map(FarmTripCommentResponse::from).getContent();
    }

    /* ============================ 小農：活動 CRUD ============================ */

    @Transactional
    public FarmTripResponse create(String farmerEmail, FarmTripRequest req) {
        Farmer farmer = getFarmer(farmerEmail);
        validatePricing(req);

        FarmTrip t = FarmTrip.builder()
                .farmer(farmer)
                .tripType(req.getTripType())
                .pricingMode(req.getPricingMode())
                .title(req.getTitle())
                .intro(req.getIntro())
                .imageUrl(req.getImageUrl())
                .location(req.getLocation())
                .price(req.getPrice())
                .capacityPerSession(req.getPricingMode() == PricingMode.PER_PERSON
                        ? req.getCapacityPerSession() : null)
                .status(FarmTripStatus.PENDING)
                .ratingCount(0)
                .ratingTotalStars(0)
                .build();
        tripRepo.save(t);

        auditRepo.save(FarmTripAudit.builder()
                .farmTrip(t)
                .status(FarmTripAuditStatus.PENDING)
                .build());

        return FarmTripResponse.from(t, List.of());
    }

    @Transactional
    public FarmTripResponse update(String farmerEmail, Long id, FarmTripRequest req) {
        FarmTrip t = tripRepo.findFullById(id)
                .orElseThrow(() -> new IllegalArgumentException("活動不存在"));
        if (!t.getFarmer().getEmail().equals(farmerEmail)) throw new AccessDeniedException("無權編輯");
        validatePricing(req);

        t.setTripType(req.getTripType());
        t.setPricingMode(req.getPricingMode());
        t.setTitle(req.getTitle());
        t.setIntro(req.getIntro());
        t.setImageUrl(req.getImageUrl());
        t.setLocation(req.getLocation());
        t.setPrice(req.getPrice());
        t.setCapacityPerSession(req.getPricingMode() == PricingMode.PER_PERSON
                ? req.getCapacityPerSession() : null);

        // 已上架可繼續編輯但若是被拒回鍋編輯後重新送審
        if (t.getStatus() == FarmTripStatus.REJECTED) {
            t.setStatus(FarmTripStatus.PENDING);
            auditRepo.save(FarmTripAudit.builder()
                    .farmTrip(t).status(FarmTripAuditStatus.PENDING).build());
        }
        return FarmTripResponse.from(t);
    }

    @Transactional
    public void close(String farmerEmail, Long id) {
        FarmTrip t = tripRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("活動不存在"));
        if (!t.getFarmer().getEmail().equals(farmerEmail)) throw new AccessDeniedException("無權下架");
        t.setStatus(FarmTripStatus.CLOSED);
        // 取消所有 ACTIVE 場次與其訂單
        for (FarmTripSession s : sessionRepo.findByFarmTripAndStatusOrderByTripStartAsc(t, FarmTripSessionStatus.ACTIVE)) {
            cancelSessionInternal(s);
        }
    }

    public PageResponse<FarmTripResponse> listFarmerOwn(String farmerEmail, Pageable pageable) {
        Farmer farmer = getFarmer(farmerEmail);
        return PageResponse.of(tripRepo.findByFarmerOrderByCreatedAtDesc(farmer, pageable), FarmTripResponse::from);
    }

    /* ============================ 小農：場次 CRUD ============================ */

    @Transactional
    public FarmTripSessionResponse addSession(String farmerEmail, Long tripId, FarmTripSessionRequest req) {
        FarmTrip t = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("活動不存在"));
        if (!t.getFarmer().getEmail().equals(farmerEmail)) throw new AccessDeniedException("無權新增場次");
        if (t.getStatus() != FarmTripStatus.ACTIVE) throw new IllegalStateException("活動需通過審核並上架才能新增場次");
        validateSessionTimes(req);

        FarmTripSession s = FarmTripSession.builder()
                .farmTrip(t)
                .sessionPrice(req.getSessionPrice())
                .tripStart(req.getTripStart())
                .tripEnd(req.getTripEnd())
                .bookStart(req.getBookStart())
                .bookEnd(req.getBookEnd())
                .attendance(0)
                .status(FarmTripSessionStatus.ACTIVE)
                .build();
        return FarmTripSessionResponse.from(sessionRepo.save(s));
    }

    /** 批次新增：選日期區間，每天建一場。已存在場次的日期跳過。 */
    @Transactional
    public FarmTripSessionBatchResponse batchAddSessions(String farmerEmail, Long tripId, FarmTripSessionBatchRequest req) {
        FarmTrip t = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("活動不存在"));
        if (!t.getFarmer().getEmail().equals(farmerEmail)) throw new AccessDeniedException("無權新增場次");
        if (t.getStatus() != FarmTripStatus.ACTIVE) throw new IllegalStateException("活動需通過審核並上架才能新增場次");

        if (!req.getDailyEndTime().isAfter(req.getDailyStartTime()))
            throw new IllegalArgumentException("每日結束時刻必須晚於開始時刻");
        if (req.getEndDate().isBefore(req.getStartDate()))
            throw new IllegalArgumentException("結束日期不可早於開始日期");

        java.time.LocalDate today = java.time.LocalDate.now();
        if (req.getStartDate().isBefore(today))
            throw new IllegalArgumentException("開始日期不可早於今天");

        java.math.BigDecimal price = req.getSessionPrice() != null ? req.getSessionPrice() : t.getPrice();
        boolean skipExisting = req.getSkipExisting() == null || req.getSkipExisting();

        // 已建場次的日期集合（含 CANCELLED 之外）
        java.util.Set<java.time.LocalDate> occupied = new java.util.HashSet<>();
        if (skipExisting) {
            for (FarmTripSession s : sessionRepo.findByFarmTripOrderByTripStartAsc(t)) {
                if (s.getStatus() != FarmTripSessionStatus.CANCELLED) {
                    occupied.add(s.getTripStart().toLocalDate());
                }
            }
        }

        java.util.List<FarmTripSession> created = new java.util.ArrayList<>();
        java.util.List<String> skipped = new java.util.ArrayList<>();

        for (java.time.LocalDate d = req.getStartDate(); !d.isAfter(req.getEndDate()); d = d.plusDays(1)) {
            if (skipExisting && occupied.contains(d)) {
                skipped.add(d.toString());
                continue;
            }
            LocalDateTime tripStart = d.atTime(req.getDailyStartTime());
            LocalDateTime tripEnd = d.atTime(req.getDailyEndTime());
            LocalDateTime bookEnd = d.minusDays(req.getBookEndDaysBefore()).atTime(23, 59);
            LocalDateTime bookStart = LocalDateTime.now();
            if (bookEnd.isAfter(tripStart)) {
                // 防呆：報名截止計算結果晚於活動開始 → 直接設成活動開始前 1 小時
                bookEnd = tripStart.minusHours(1);
            }
            if (bookEnd.isBefore(bookStart)) {
                // 已經來不及收報名，跳過
                skipped.add(d.toString());
                continue;
            }
            FarmTripSession s = sessionRepo.save(FarmTripSession.builder()
                    .farmTrip(t)
                    .sessionPrice(price)
                    .tripStart(tripStart)
                    .tripEnd(tripEnd)
                    .bookStart(bookStart)
                    .bookEnd(bookEnd)
                    .attendance(0)
                    .status(FarmTripSessionStatus.ACTIVE)
                    .build());
            created.add(s);
        }

        return FarmTripSessionBatchResponse.builder()
                .created(created.stream().map(FarmTripSessionResponse::from).toList())
                .skipped(skipped)
                .build();
    }

    @Transactional
    public FarmTripSessionResponse updateSession(String farmerEmail, Long sessionId, FarmTripSessionRequest req) {
        FarmTripSession s = sessionRepo.findFullById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("場次不存在"));
        if (!s.getFarmTrip().getFarmer().getEmail().equals(farmerEmail)) throw new AccessDeniedException("無權編輯");
        if (s.getStatus() == FarmTripSessionStatus.CANCELLED) throw new IllegalStateException("已取消的場次無法編輯");
        validateSessionTimes(req);

        s.setSessionPrice(req.getSessionPrice());
        s.setTripStart(req.getTripStart());
        s.setTripEnd(req.getTripEnd());
        s.setBookStart(req.getBookStart());
        s.setBookEnd(req.getBookEnd());
        return FarmTripSessionResponse.from(s);
    }

    @Transactional
    public void cancelSession(String farmerEmail, Long sessionId) {
        FarmTripSession s = sessionRepo.findFullById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("場次不存在"));
        if (!s.getFarmTrip().getFarmer().getEmail().equals(farmerEmail)) throw new AccessDeniedException("無權取消");
        cancelSessionInternal(s);
    }

    private void cancelSessionInternal(FarmTripSession s) {
        if (s.getStatus() == FarmTripSessionStatus.CANCELLED) return;
        s.setStatus(FarmTripSessionStatus.CANCELLED);
        for (FarmTripOrder o : orderRepo.findBySessionAndStatus(s, FarmTripOrderStatus.CONFIRMED)) {
            o.setStatus(FarmTripOrderStatus.CANCELLED);
            o.setCancelledAt(LocalDateTime.now());
        }
    }

    /* ============================ 消費者：預約 ============================ */

    @Transactional
    public FarmTripOrderResponse bookSession(String email, Long sessionId, CreateFarmTripOrderRequest req) {
        Member user = getMember(email);
        FarmTripSession s = sessionRepo.findFullById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("場次不存在"));
        FarmTrip t = s.getFarmTrip();

        if (t.getStatus() != FarmTripStatus.ACTIVE) throw new IllegalStateException("活動未上架");
        if (s.getStatus() == FarmTripSessionStatus.CANCELLED) throw new IllegalStateException("場次已取消");
        if (s.getStatus() == FarmTripSessionStatus.COMPLETED) throw new IllegalStateException("場次已截止");

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(s.getBookStart())) throw new IllegalStateException("尚未開始報名");
        if (now.isAfter(s.getBookEnd())) throw new IllegalStateException("已過報名截止");

        // PER_PERSON 才檢查容量
        if (t.getPricingMode() == PricingMode.PER_PERSON) {
            Integer cap = t.getCapacityPerSession();
            if (cap != null && s.getAttendance() + req.getNumPeople() > cap) {
                throw new IllegalArgumentException("剩餘名額僅 " + Math.max(0, cap - s.getAttendance()) + " 位");
            }
        }

        BigDecimal unitPrice = s.getSessionPrice();
        BigDecimal total = (t.getPricingMode() == PricingMode.PER_PERSON)
                ? unitPrice.multiply(BigDecimal.valueOf(req.getNumPeople()))
                : BigDecimal.ZERO; // PER_WEIGHT 等小農結算

        FarmTripOrder o = FarmTripOrder.builder()
                .bookingNo(generateBookingNo())
                .session(s)
                .user(user)
                .numPeople(req.getNumPeople())
                .unitPrice(unitPrice)
                .totalAmount(total)
                .contactName(req.getContactName())
                .contactPhone(req.getContactPhone())
                .note(req.getNote())
                .status(FarmTripOrderStatus.CONFIRMED)
                .build();
        orderRepo.save(o);

        s.setAttendance(s.getAttendance() + req.getNumPeople());
        return FarmTripOrderResponse.from(o);
    }

    @Transactional
    public FarmTripOrderResponse cancelOrder(String email, Long orderId) {
        FarmTripOrder o = orderRepo.findFullById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("預約不存在"));
        if (!o.getUser().getEmail().equals(email)) throw new AccessDeniedException("無權取消此預約");
        if (o.getStatus() == FarmTripOrderStatus.CANCELLED) throw new IllegalStateException("已取消");
        if (o.getStatus() == FarmTripOrderStatus.COMPLETED) throw new IllegalStateException("已完成，無法取消");

        FarmTripSession s = o.getSession();
        if (LocalDateTime.now().isAfter(s.getTripStart())) {
            throw new IllegalStateException("活動已開始，無法取消");
        }

        o.setStatus(FarmTripOrderStatus.CANCELLED);
        o.setCancelledAt(LocalDateTime.now());
        s.setAttendance(Math.max(0, s.getAttendance() - o.getNumPeople()));
        return FarmTripOrderResponse.from(o);
    }

    public PageResponse<FarmTripOrderResponse> myOrders(String email, Pageable pageable) {
        Member me = getMember(email);
        return PageResponse.of(orderRepo.findByUserOrderByBookedAtDesc(me, pageable), FarmTripOrderResponse::from);
    }

    public PageResponse<FarmTripOrderResponse> farmerOrders(String farmerEmail, FarmTripOrderStatus status, Pageable pageable) {
        Farmer farmer = getFarmer(farmerEmail);
        Page<FarmTripOrder> page = (status != null)
                ? orderRepo.findBySession_FarmTrip_FarmerAndStatusOrderByBookedAtDesc(farmer, status, pageable)
                : orderRepo.findBySession_FarmTrip_FarmerOrderByBookedAtDesc(farmer, pageable);
        return PageResponse.of(page, FarmTripOrderResponse::from);
    }

    /** 小農補登實際採收重量並完成訂單；PER_PERSON 也可手動完成（會直接以下單金額作結） */
    @Transactional
    public FarmTripOrderResponse completeOrder(String farmerEmail, Long orderId, FarmTripCompleteRequest req) {
        FarmTripOrder o = orderRepo.findFullById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("預約不存在"));
        if (!o.getSession().getFarmTrip().getFarmer().getEmail().equals(farmerEmail)) {
            throw new AccessDeniedException("無權處理此訂單");
        }
        if (o.getStatus() != FarmTripOrderStatus.CONFIRMED) {
            throw new IllegalStateException("僅 CONFIRMED 訂單可完成");
        }
        if (o.getSession().getFarmTrip().isPerWeight()) {
            if (req.getActualWeight() == null || req.getActualWeight().signum() <= 0) {
                throw new IllegalArgumentException("採重量計價需填寫實際採收公斤數");
            }
            o.setActualWeight(req.getActualWeight());
            o.setTotalAmount(o.getUnitPrice().multiply(req.getActualWeight()));
        }
        o.setStatus(FarmTripOrderStatus.COMPLETED);
        o.setCompletedAt(LocalDateTime.now());
        return FarmTripOrderResponse.from(o);
    }

    /* ============================ 評論 ============================ */

    @Transactional
    public FarmTripCommentResponse comment(String email, Long tripId, FarmTripCommentRequest req) {
        Member user = getMember(email);
        FarmTrip t = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("活動不存在"));

        // 必須有此活動的 COMPLETED 訂單
        if (!orderRepo.existsBySession_FarmTripAndUserAndStatus(t, user, FarmTripOrderStatus.COMPLETED)) {
            throw new IllegalStateException("只有實際參加完此活動才能評論");
        }
        // 不可重複評論
        if (commentRepo.existsByFarmTripAndUser(t, user)) {
            throw new IllegalStateException("您已評論過此活動");
        }

        FarmTripComment c = FarmTripComment.builder()
                .farmTrip(t).user(user)
                .rating(req.getRating()).content(req.getContent())
                .build();
        commentRepo.save(c);
        t.setRatingCount(t.getRatingCount() + 1);
        t.setRatingTotalStars(t.getRatingTotalStars() + req.getRating());
        return FarmTripCommentResponse.from(c);
    }

    /* ============================ 管理員：審核 ============================ */

    public PageResponse<FarmTripResponse> adminListPending(Pageable pageable) {
        return PageResponse.of(
                tripRepo.findByStatusOrderByCreatedAtDesc(FarmTripStatus.PENDING, pageable),
                FarmTripResponse::from);
    }

    @Transactional
    public FarmTripAuditResponse adminAudit(String adminEmail, Long tripId, FarmTripAuditRequest req) {
        if (req.getDecision() == FarmTripAuditStatus.PENDING) {
            throw new IllegalArgumentException("審核結果只能是 APPROVED 或 REJECTED");
        }
        Admin admin = adminRepo.findByEmail(adminEmail)
                .orElseThrow(() -> new AccessDeniedException("管理員帳號不存在"));
        FarmTrip t = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("活動不存在"));

        FarmTripAudit audit = auditRepo.findFirstByFarmTripAndStatusOrderByCreatedAtDesc(t, FarmTripAuditStatus.PENDING)
                .orElseGet(() -> auditRepo.save(FarmTripAudit.builder()
                        .farmTrip(t).status(FarmTripAuditStatus.PENDING).build()));
        audit.setAdmin(admin);
        audit.setStatus(req.getDecision());
        audit.setReason(req.getReason());

        t.setStatus(req.getDecision() == FarmTripAuditStatus.APPROVED
                ? FarmTripStatus.ACTIVE : FarmTripStatus.REJECTED);
        return FarmTripAuditResponse.from(audit);
    }

    public List<FarmTripAuditResponse> auditHistory(Long tripId) {
        FarmTrip t = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("活動不存在"));
        return auditRepo.findByFarmTripOrderByCreatedAtDesc(t).stream()
                .map(FarmTripAuditResponse::from).toList();
    }

    /* ============================ 排程 ============================ */

    /** 過期場次→COMPLETED；對應 CONFIRMED 訂單→COMPLETED (PER_PERSON 自動以下單金額作結) */
    @Scheduled(fixedDelayString = "PT300S", initialDelayString = "PT30S")
    @Transactional
    public void refreshSessionStatus() {
        LocalDateTime now = LocalDateTime.now();
        for (FarmTripSession s : sessionRepo.findByStatusAndTripEndBefore(FarmTripSessionStatus.ACTIVE, now)) {
            s.setStatus(FarmTripSessionStatus.COMPLETED);
            for (FarmTripOrder o : orderRepo.findBySessionAndStatus(s, FarmTripOrderStatus.CONFIRMED)) {
                if (!s.getFarmTrip().isPerWeight()) {
                    o.setStatus(FarmTripOrderStatus.COMPLETED);
                    o.setCompletedAt(now);
                }
                // PER_WEIGHT 不自動結算，等小農補登重量
            }
        }
    }

    /* ============================ helpers ============================ */

    private void validatePricing(FarmTripRequest r) {
        if (r.getPricingMode() == PricingMode.PER_PERSON
                && (r.getCapacityPerSession() == null || r.getCapacityPerSession() < 1)) {
            throw new IllegalArgumentException("每人計價需指定場次容量 (>=1)");
        }
    }

    private void validateSessionTimes(FarmTripSessionRequest r) {
        if (!r.getTripEnd().isAfter(r.getTripStart())) throw new IllegalArgumentException("活動結束必須晚於開始");
        if (!r.getBookEnd().isAfter(r.getBookStart())) throw new IllegalArgumentException("報名截止必須晚於開放報名");
        if (r.getBookEnd().isAfter(r.getTripStart())) throw new IllegalArgumentException("報名截止不可晚於活動開始");
    }

    private Member getMember(String email) {
        return memberRepo.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("會員帳號不存在"));
    }

    private Farmer getFarmer(String email) {
        return farmerRepo.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("小農帳號不存在"));
    }

    private String generateBookingNo() {
        String date = LocalDate.now().format(DATE_FMT);
        for (int i = 0; i < 5; i++) {
            String no = "FT-" + date + "-" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
            if (!orderRepo.existsByBookingNo(no)) return no;
        }
        return "FT-" + date + "-" + System.currentTimeMillis() % 100000;
    }
}
