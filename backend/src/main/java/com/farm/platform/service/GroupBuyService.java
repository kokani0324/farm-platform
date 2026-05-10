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
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GroupBuyService {

    private final GroupBuyRequestRepository requestRepo;
    private final GroupBuyRepository groupBuyRepo;
    private final GroupBuyParticipationRepository participationRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final OrderRepository orderRepo;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /* ============================ 發起 / 撤回 ============================ */

    @Transactional
    public GroupBuyRequestResponse createRequest(String email, CreateGroupBuyRequest req) {
        User initiator = getUser(email);

        Product product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalArgumentException("此商品目前無法發起團購");
        }
        if (Boolean.FALSE.equals(product.getGroupBuyEnabled())) {
            throw new IllegalArgumentException("此商品未開放團購");
        }
        if (req.getGroupPrice().compareTo(product.getPrice()) >= 0) {
            throw new IllegalArgumentException("團購價必須低於原價(NT$ " + product.getPrice() + ")");
        }
        if (!req.getDeadlineDate().isAfter(req.getOpenDate())) {
            throw new IllegalArgumentException("截止時間必須晚於開團時間");
        }

        GroupBuyRequest gbr = GroupBuyRequest.builder()
                .product(product)
                .initiator(initiator)
                .farmer(product.getFarmer())
                .targetQuantity(req.getTargetQuantity())
                .groupPrice(req.getGroupPrice())
                .openDate(req.getOpenDate())
                .deadlineDate(req.getDeadlineDate())
                .message(req.getMessage())
                .status(GroupBuyRequestStatus.PENDING)
                .build();
        GroupBuyRequest saved = requestRepo.save(gbr);
        return GroupBuyRequestResponse.from(saved, null);
    }

    @Transactional
    public GroupBuyRequestResponse withdrawRequest(String email, Long requestId) {
        GroupBuyRequest r = requestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("請求不存在"));
        if (!r.getInitiator().getEmail().equals(email)) {
            throw new AccessDeniedException("無權撤回此請求");
        }
        if (r.getStatus() != GroupBuyRequestStatus.PENDING) {
            throw new IllegalStateException("只能撤回審核中的請求");
        }
        r.setStatus(GroupBuyRequestStatus.WITHDRAWN);
        return GroupBuyRequestResponse.from(r, null);
    }

    /* ============================ 小農審核 ============================ */

    @Transactional
    public GroupBuyRequestResponse review(String farmerEmail, Long requestId, ReviewGroupBuyRequest body) {
        GroupBuyRequest r = requestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("請求不存在"));
        if (!r.getFarmer().getEmail().equals(farmerEmail)) {
            throw new AccessDeniedException("非該商品的小農");
        }
        if (r.getStatus() != GroupBuyRequestStatus.PENDING) {
            throw new IllegalStateException("此請求已處理");
        }

        Long createdGroupBuyId = null;
        if (Boolean.TRUE.equals(body.getApproved())) {
            r.setStatus(GroupBuyRequestStatus.APPROVED);
            GroupBuy gb = GroupBuy.builder()
                    .request(r)
                    .product(r.getProduct())
                    .host(r.getInitiator())
                    .farmer(r.getFarmer())
                    .targetQuantity(r.getTargetQuantity())
                    .groupPrice(r.getGroupPrice())
                    .openDate(r.getOpenDate())
                    .deadlineDate(r.getDeadlineDate())
                    .status(GroupBuyStatus.OPEN)
                    .build();
            createdGroupBuyId = groupBuyRepo.save(gb).getId();
        } else {
            r.setStatus(GroupBuyRequestStatus.REJECTED);
            r.setRejectReason(body.getRejectReason());
        }
        r.setRepliedAt(LocalDateTime.now());
        return GroupBuyRequestResponse.from(r, createdGroupBuyId);
    }

    /* ============================ 列表查詢 ============================ */

    public PageResponse<GroupBuyRequestResponse> myRequests(String email, Pageable pageable) {
        User me = getUser(email);
        Page<GroupBuyRequest> page = requestRepo.findByInitiatorOrderByRequestedAtDesc(me, pageable);
        return PageResponse.of(page, r -> GroupBuyRequestResponse.from(r, null));
    }

    public PageResponse<GroupBuyRequestResponse> farmerRequests(String email, GroupBuyRequestStatus status, Pageable pageable) {
        User me = getUser(email);
        if (!me.hasRole(Role.FARMER)) throw new AccessDeniedException("非小農");
        Page<GroupBuyRequest> page = (status != null)
                ? requestRepo.findByFarmerAndStatusOrderByRequestedAtDesc(me, status, pageable)
                : requestRepo.findByFarmerOrderByRequestedAtDesc(me, pageable);
        return PageResponse.of(page, r -> GroupBuyRequestResponse.from(r, null));
    }

    public PageResponse<GroupBuyResponse> listOpen(Pageable pageable, String viewerEmail) {
        Page<GroupBuy> page = groupBuyRepo.findByStatusOrderByDeadlineDateAsc(GroupBuyStatus.OPEN, pageable);
        User viewer = (viewerEmail != null) ? userRepo.findByEmail(viewerEmail).orElse(null) : null;
        return PageResponse.of(page, gb -> toResponse(gb, viewer));
    }

    public PageResponse<GroupBuyResponse> farmerGroupBuys(String email, Pageable pageable) {
        User me = getUser(email);
        if (!me.hasRole(Role.FARMER)) throw new AccessDeniedException("非小農");
        Page<GroupBuy> page = groupBuyRepo.findByFarmerOrderByCreatedAtDesc(me, pageable);
        return PageResponse.of(page, gb -> toResponse(gb, me));
    }

    public GroupBuyResponse getDetail(Long id, String viewerEmail) {
        GroupBuy gb = groupBuyRepo.findFullById(id)
                .orElseThrow(() -> new IllegalArgumentException("團購不存在"));
        User viewer = (viewerEmail != null) ? userRepo.findByEmail(viewerEmail).orElse(null) : null;
        return toResponse(gb, viewer);
    }

    /* ============================ 加入 / 退出 ============================ */

    @Transactional
    public ParticipationResponse join(String email, Long groupBuyId, JoinGroupBuyRequest req) {
        User me = getUser(email);
        GroupBuy gb = groupBuyRepo.findFullById(groupBuyId)
                .orElseThrow(() -> new IllegalArgumentException("團購不存在"));

        if (gb.getStatus() != GroupBuyStatus.OPEN) {
            throw new IllegalStateException("此團購已結束");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(gb.getOpenDate())) {
            throw new IllegalStateException("團購尚未開始");
        }
        if (now.isAfter(gb.getDeadlineDate())) {
            throw new IllegalStateException("團購已截止");
        }

        Optional<GroupBuyParticipation> existing = participationRepo.findByGroupBuyAndUser(gb, me);
        GroupBuyParticipation p;
        if (existing.isPresent()) {
            p = existing.get();
            if (p.getStatus() == ParticipationStatus.JOINED) {
                throw new IllegalStateException("您已加入此團購,如需修改請先退出再重新加入");
            }
            // 重新加入(從 WITHDRAWN 變回 JOINED)
            p.setStatus(ParticipationStatus.JOINED);
            p.setQuantity(req.getQuantity());
            p.setSubtotal(gb.getGroupPrice().multiply(BigDecimal.valueOf(req.getQuantity())));
            p.setRecipientName(req.getRecipientName());
            p.setRecipientPhone(req.getRecipientPhone());
            p.setShippingAddress(req.getShippingAddress());
            p.setNote(req.getNote());
        } else {
            BigDecimal subtotal = gb.getGroupPrice().multiply(BigDecimal.valueOf(req.getQuantity()));
            p = GroupBuyParticipation.builder()
                    .groupBuy(gb)
                    .user(me)
                    .isHost(me.getId().equals(gb.getHost().getId()))
                    .quantity(req.getQuantity())
                    .subtotal(subtotal)
                    .recipientName(req.getRecipientName())
                    .recipientPhone(req.getRecipientPhone())
                    .shippingAddress(req.getShippingAddress())
                    .note(req.getNote())
                    .status(ParticipationStatus.JOINED)
                    .build();
            participationRepo.save(p);
        }
        return ParticipationResponse.from(p);
    }

    @Transactional
    public ParticipationResponse withdraw(String email, Long groupBuyId) {
        User me = getUser(email);
        GroupBuy gb = groupBuyRepo.findById(groupBuyId)
                .orElseThrow(() -> new IllegalArgumentException("團購不存在"));
        if (gb.getStatus() != GroupBuyStatus.OPEN) {
            throw new IllegalStateException("已結束的團購無法退出");
        }
        if (LocalDateTime.now().isAfter(gb.getDeadlineDate())) {
            throw new IllegalStateException("團購已截止,無法退出");
        }
        GroupBuyParticipation p = participationRepo.findByGroupBuyAndUser(gb, me)
                .orElseThrow(() -> new IllegalStateException("您未參加此團購"));
        if (p.getStatus() == ParticipationStatus.WITHDRAWN) {
            throw new IllegalStateException("已退出");
        }
        p.setStatus(ParticipationStatus.WITHDRAWN);
        return ParticipationResponse.from(p);
    }

    public PageResponse<ParticipationResponse> myParticipations(String email, Pageable pageable) {
        User me = getUser(email);
        Page<GroupBuyParticipation> page = participationRepo.findByUserOrderByJoinedAtDesc(me, pageable);
        return PageResponse.of(page, ParticipationResponse::from);
    }

    /* ============================ 排程: 截止判定 ============================ */

    /** 每分鐘檢查到期的團購 */
    @Scheduled(fixedDelayString = "PT60S", initialDelayString = "PT10S")
    @Transactional
    public void closeExpiredGroupBuys() {
        List<GroupBuy> expired = groupBuyRepo.findByStatusAndDeadlineDateBefore(GroupBuyStatus.OPEN, LocalDateTime.now());
        if (expired.isEmpty()) return;

        for (GroupBuy gb : expired) {
            try {
                int joinedQty = participationRepo.sumQuantityByGroupBuy(gb, ParticipationStatus.JOINED);
                if (joinedQty >= gb.getTargetQuantity()) {
                    settleSuccess(gb);
                } else {
                    gb.setStatus(GroupBuyStatus.FAILED);
                    log.info("[GroupBuy] #{} 未達標({}/{}) → FAILED", gb.getId(), joinedQty, gb.getTargetQuantity());
                }
            } catch (Exception ex) {
                log.error("[GroupBuy] 結算 #{} 失敗: {}", gb.getId(), ex.getMessage(), ex);
            }
        }
    }

    /** 成團處理:扣庫存 + 為每位 JOINED 參與者建訂單 */
    private void settleSuccess(GroupBuy gb) {
        List<GroupBuyParticipation> joined = participationRepo.findByGroupBuyAndStatus(gb, ParticipationStatus.JOINED);
        int totalQty = joined.stream().mapToInt(GroupBuyParticipation::getQuantity).sum();

        Product product = gb.getProduct();
        if (product.getStock() < totalQty) {
            // 庫存不足 → 視為失敗
            gb.setStatus(GroupBuyStatus.FAILED);
            log.warn("[GroupBuy] #{} 達標但庫存不足({}<{}) → FAILED", gb.getId(), product.getStock(), totalQty);
            return;
        }

        // 扣庫存
        product.setStock(product.getStock() - totalQty);
        if (product.getStock() == 0) product.setStatus(ProductStatus.SOLD_OUT);

        for (GroupBuyParticipation p : joined) {
            Order order = Order.builder()
                    .orderNo(generateOrderNo())
                    .consumer(p.getUser())
                    .farmer(gb.getFarmer())
                    .status(OrderStatus.PENDING_PAYMENT)
                    .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                    .recipientName(p.getRecipientName())
                    .recipientPhone(p.getRecipientPhone())
                    .shippingAddress(p.getShippingAddress())
                    .note((p.getNote() == null ? "" : p.getNote() + " / ") + "(來自團購 #" + gb.getId() + ")")
                    .totalAmount(p.getSubtotal())
                    .groupBuy(gb)
                    .build();

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .unit(product.getUnit())
                    .imageUrl(product.getImageUrl())
                    .unitPrice(gb.getGroupPrice())
                    .quantity(p.getQuantity())
                    .subtotal(p.getSubtotal())
                    .build();
            order.addItem(item);
            Order saved = orderRepo.save(order);
            p.setOrder(saved);
        }

        gb.setStatus(GroupBuyStatus.SUCCESS);
        log.info("[GroupBuy] #{} 成團 {}/{} → 建立 {} 張訂單", gb.getId(), totalQty, gb.getTargetQuantity(), joined.size());
    }

    /* ============================ helpers ============================ */

    private GroupBuyResponse toResponse(GroupBuy gb, User viewer) {
        int currentQty = participationRepo.sumQuantityByGroupBuy(gb, ParticipationStatus.JOINED);
        Boolean joined = null;
        Integer myQty = null;
        if (viewer != null) {
            Optional<GroupBuyParticipation> mine = participationRepo.findByGroupBuyAndUser(gb, viewer);
            joined = mine.map(p -> p.getStatus() == ParticipationStatus.JOINED).orElse(false);
            myQty = mine.map(GroupBuyParticipation::getQuantity).orElse(null);
        }
        return GroupBuyResponse.from(gb, currentQty, joined, myQty);
    }

    private User getUser(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("使用者不存在"));
    }

    private String generateOrderNo() {
        String date = LocalDate.now().format(DATE_FMT);
        for (int i = 0; i < 5; i++) {
            String no = "GB-" + date + "-" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
            if (!orderRepo.existsByOrderNo(no)) return no;
        }
        return "GB-" + date + "-" + System.currentTimeMillis() % 100000;
    }
}
