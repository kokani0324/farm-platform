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
    private final GroupBuyOrderRepository groupBuyOrderRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

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
        BigDecimal subtotal = gb.getGroupPrice().multiply(BigDecimal.valueOf(req.getQuantity()));
        LocalDateTime nowTs = LocalDateTime.now();
        if (existing.isPresent()) {
            p = existing.get();
            if (p.getStatus() == ParticipationStatus.JOINED) {
                throw new IllegalStateException("您已加入此團購,如需修改請先退出再重新加入");
            }
            // 重新加入(從 WITHDRAWN 變回 JOINED)
            p.setStatus(ParticipationStatus.JOINED);
            p.setQuantity(req.getQuantity());
            p.setSubtotal(subtotal);
            p.setRecipientName(req.getRecipientName());
            p.setRecipientPhone(req.getRecipientPhone());
            p.setShippingZipcode(req.getShippingZipcode());
            p.setShippingCity(req.getShippingCity());
            p.setShippingDist(req.getShippingDist());
            p.setShippingDetail(req.getShippingDetail());
            p.setNote(req.getNote());
            // 重新加入也視為一次新的付款（mock）
            p.setPaymentStatus(PaymentStatus.PAID);
            p.setPaymentMethod(req.getPaymentMethod());
            p.setPaidAt(nowTs);
            p.setRefundedAt(null);
        } else {
            p = GroupBuyParticipation.builder()
                    .groupBuy(gb)
                    .user(me)
                    .isHost(me.getId().equals(gb.getHost().getId()))
                    .quantity(req.getQuantity())
                    .subtotal(subtotal)
                    .recipientName(req.getRecipientName())
                    .recipientPhone(req.getRecipientPhone())
                    .shippingZipcode(req.getShippingZipcode())
                    .shippingCity(req.getShippingCity())
                    .shippingDist(req.getShippingDist())
                    .shippingDetail(req.getShippingDetail())
                    .note(req.getNote())
                    .status(ParticipationStatus.JOINED)
                    .paymentStatus(PaymentStatus.PAID)
                    .paymentMethod(req.getPaymentMethod())
                    .paidAt(nowTs)
                    .receiptStatus(ReceiptStatus.NOT_SHIPPED)
                    .build();
            participationRepo.save(p);
        }
        log.info("[GroupBuy] #{} user={} 加入團購並完成付款({})", gb.getId(), me.getEmail(), req.getPaymentMethod());
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
        // 退出視為退款（mock）
        if (p.getPaymentStatus() == PaymentStatus.PAID) {
            p.setPaymentStatus(PaymentStatus.REFUNDED);
            p.setRefundedAt(LocalDateTime.now());
            log.info("[GroupBuy] #{} user={} 退出 → 退款", gb.getId(), me.getEmail());
        }
        return ParticipationResponse.from(p);
    }

    public PageResponse<ParticipationResponse> myParticipations(String email, Pageable pageable) {
        User me = getUser(email);
        Page<GroupBuyParticipation> page = participationRepo.findByUserOrderByJoinedAtDesc(me, pageable);
        return PageResponse.of(page, ParticipationResponse::from);
    }

    /* ============================ 團購整單（GroupBuyOrder） ============================ */

    /** 團主的整單列表 */
    public PageResponse<GroupBuyOrderResponse> myHostedOrders(String hostEmail, Pageable pageable) {
        User me = getUser(hostEmail);
        Page<GroupBuyOrder> page = groupBuyOrderRepo.findByHostOrderByCreatedAtDesc(me, pageable);
        return PageResponse.of(page, gbo -> GroupBuyOrderResponse.from(gbo, List.of()));
    }

    /** 小農名下的整單列表 */
    public PageResponse<GroupBuyOrderResponse> farmerGroupBuyOrders(String farmerEmail, Pageable pageable) {
        User me = getUser(farmerEmail);
        if (!me.hasRole(Role.FARMER)) throw new AccessDeniedException("非小農");
        Page<GroupBuyOrder> page = groupBuyOrderRepo.findByFarmerOrderByCreatedAtDesc(me, pageable);
        return PageResponse.of(page, gbo -> GroupBuyOrderResponse.from(gbo, List.of()));
    }

    /** 取得某團購的整單（團主、小農、參與成員可看） */
    public GroupBuyOrderResponse getGroupBuyOrder(String viewerEmail, Long groupBuyId) {
        User me = getUser(viewerEmail);
        GroupBuy gb = groupBuyRepo.findById(groupBuyId)
                .orElseThrow(() -> new IllegalArgumentException("團購不存在"));
        GroupBuyOrder gbo = groupBuyOrderRepo.findByGroupBuy(gb)
                .orElseThrow(() -> new IllegalStateException("尚未成團或團購結算未完成"));

        boolean isHost = gbo.getHost().getId().equals(me.getId());
        boolean isFarmer = gbo.getFarmer().getId().equals(me.getId());
        boolean isMember = participationRepo.findByGroupBuyAndUser(gb, me).isPresent();
        if (!isHost && !isFarmer && !isMember && !me.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("無權檢視此團購整單");
        }

        List<GroupBuyParticipation> joined = participationRepo.findByGroupBuyAndStatus(gb, ParticipationStatus.JOINED);
        List<ParticipationResponse> partsDto = joined.stream().map(ParticipationResponse::from).toList();
        return GroupBuyOrderResponse.from(gbo, partsDto);
    }

    /** 小農標記某位團員的 participation 已出貨 */
    @Transactional
    public ParticipationResponse markParticipationShipped(String farmerEmail, Long groupBuyId, Long participationId) {
        User me = getUser(farmerEmail);
        GroupBuy gb = groupBuyRepo.findById(groupBuyId)
                .orElseThrow(() -> new IllegalArgumentException("團購不存在"));
        if (!gb.getFarmer().getId().equals(me.getId())) {
            throw new AccessDeniedException("非該團購的小農");
        }
        GroupBuyParticipation p = participationRepo.findById(participationId)
                .orElseThrow(() -> new IllegalArgumentException("參與紀錄不存在"));
        if (!p.getGroupBuy().getId().equals(gb.getId())) {
            throw new IllegalArgumentException("此參與紀錄不屬於該團購");
        }
        if (p.getStatus() != ParticipationStatus.JOINED) {
            throw new IllegalStateException("該團員未成功加入此團購");
        }

        GroupBuyOrder gbo = p.getGroupBuyOrder();
        if (gbo == null) {
            throw new IllegalStateException("整單尚未建立，無法出貨");
        }
        if (gbo.getStatus() != GroupBuyOrderStatus.PAID && gbo.getStatus() != GroupBuyOrderStatus.SHIPPING) {
            throw new IllegalStateException("整單尚未付款，不可出貨");
        }
        if (p.getReceiptStatus() != ReceiptStatus.NOT_SHIPPED) {
            throw new IllegalStateException("此筆已出貨或已收貨");
        }
        p.setReceiptStatus(ReceiptStatus.SHIPPED);
        p.setShippedAt(LocalDateTime.now());

        if (gbo.getStatus() == GroupBuyOrderStatus.PAID) {
            gbo.setStatus(GroupBuyOrderStatus.SHIPPING);
        }
        log.info("[GBO] #{} participation #{} 已出貨", gbo.getId(), p.getId());
        return ParticipationResponse.from(p);
    }

    /** 團員標記自己的 participation 已收貨 */
    @Transactional
    public ParticipationResponse markMyReceiptReceived(String email, Long groupBuyId) {
        User me = getUser(email);
        GroupBuy gb = groupBuyRepo.findById(groupBuyId)
                .orElseThrow(() -> new IllegalArgumentException("團購不存在"));
        GroupBuyParticipation p = participationRepo.findByGroupBuyAndUser(gb, me)
                .orElseThrow(() -> new IllegalStateException("您未參加此團購"));
        if (p.getStatus() != ParticipationStatus.JOINED) {
            throw new IllegalStateException("您未成功加入此團購");
        }
        if (p.getReceiptStatus() != ReceiptStatus.SHIPPED) {
            throw new IllegalStateException("商品尚未出貨或已確認收貨");
        }
        p.setReceiptStatus(ReceiptStatus.RECEIVED);
        p.setReceiptDatetime(LocalDateTime.now());

        // 若所有 JOINED 參與者都已 RECEIVED → 整單 COMPLETED
        GroupBuyOrder gbo = p.getGroupBuyOrder();
        if (gbo != null) {
            List<GroupBuyParticipation> all = participationRepo.findByGroupBuyAndStatus(gb, ParticipationStatus.JOINED);
            boolean allReceived = all.stream().allMatch(x -> x.getReceiptStatus() == ReceiptStatus.RECEIVED);
            if (allReceived) {
                gbo.setStatus(GroupBuyOrderStatus.COMPLETED);
                gbo.setCompletedAt(LocalDateTime.now());
                log.info("[GBO] #{} 全部團員已收貨 → COMPLETED", gbo.getId());
            }
        }
        return ParticipationResponse.from(p);
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
                    settleFailed(gb, joinedQty);
                }
            } catch (Exception ex) {
                log.error("[GroupBuy] 結算 #{} 失敗: {}", gb.getId(), ex.getMessage(), ex);
            }
        }
    }

    /** 未達標：標記 FAILED + 退款所有已付款的團員 */
    private void settleFailed(GroupBuy gb, int joinedQty) {
        gb.setStatus(GroupBuyStatus.FAILED);
        List<GroupBuyParticipation> joined = participationRepo.findByGroupBuyAndStatus(gb, ParticipationStatus.JOINED);
        LocalDateTime nowTs = LocalDateTime.now();
        int refunded = 0;
        for (GroupBuyParticipation p : joined) {
            if (p.getPaymentStatus() == PaymentStatus.PAID) {
                p.setPaymentStatus(PaymentStatus.REFUNDED);
                p.setRefundedAt(nowTs);
                refunded++;
            }
        }
        log.info("[GroupBuy] #{} 未達標({}/{}) → FAILED, 退款 {} 筆", gb.getId(), joinedQty, gb.getTargetQuantity(), refunded);
    }

    /** 成團處理：扣庫存 + 為整團建立一張 GroupBuyOrder（屬團主），所有團員 participation 連到此整單 */
    private void settleSuccess(GroupBuy gb) {
        List<GroupBuyParticipation> joined = participationRepo.findByGroupBuyAndStatus(gb, ParticipationStatus.JOINED);
        int totalQty = joined.stream().mapToInt(GroupBuyParticipation::getQuantity).sum();
        BigDecimal totalAmount = joined.stream()
                .map(GroupBuyParticipation::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Product product = gb.getProduct();
        if (product.getStock() < totalQty) {
            gb.setStatus(GroupBuyStatus.FAILED);
            log.warn("[GroupBuy] #{} 達標但庫存不足({}<{}) → FAILED", gb.getId(), product.getStock(), totalQty);
            return;
        }

        product.setStock(product.getStock() - totalQty);
        if (product.getStock() == 0) product.setStatus(ProductStatus.SOLD_OUT);

        // 加入時已逐筆付款，整單成立即視為 PAID（待出貨）
        GroupBuyOrder gbo = GroupBuyOrder.builder()
                .orderNo(generateGroupBuyOrderNo())
                .groupBuy(gb)
                .host(gb.getHost())
                .farmer(gb.getFarmer())
                .totalQuantity(totalQty)
                .totalAmount(totalAmount)
                .status(GroupBuyOrderStatus.PAID)
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .paidAt(LocalDateTime.now())
                .build();
        GroupBuyOrder savedGbo = groupBuyOrderRepo.save(gbo);

        for (GroupBuyParticipation p : joined) {
            p.setGroupBuyOrder(savedGbo);
            p.setReceiptStatus(ReceiptStatus.NOT_SHIPPED);
        }

        gb.setStatus(GroupBuyStatus.SUCCESS);
        log.info("[GroupBuy] #{} 成團 {}/{} → 建立整單 GBO#{}（總額 {}）",
                gb.getId(), totalQty, gb.getTargetQuantity(), savedGbo.getId(), totalAmount);
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

    private String generateGroupBuyOrderNo() {
        String date = LocalDate.now().format(DATE_FMT);
        for (int i = 0; i < 5; i++) {
            String no = "GBO-" + date + "-" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
            if (!groupBuyOrderRepo.existsByOrderNo(no)) return no;
        }
        return "GBO-" + date + "-" + System.currentTimeMillis() % 100000;
    }
}
