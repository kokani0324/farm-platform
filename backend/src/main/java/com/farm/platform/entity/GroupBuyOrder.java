package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 成團後針對「整個團」建立的整單，1:1 對應 GroupBuy。
 * 團主名下，金額為所有 JOINED 團員 subtotal 加總；
 * 各團員的收件資訊、收貨狀態仍記錄於 {@link GroupBuyParticipation}。
 */
@Entity
@Table(name = "group_buy_orders", indexes = {
        @Index(name = "idx_gbo_groupbuy", columnList = "group_buy_id", unique = true),
        @Index(name = "idx_gbo_host", columnList = "host_id"),
        @Index(name = "idx_gbo_farmer", columnList = "farmer_id"),
        @Index(name = "idx_gbo_status", columnList = "status"),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupBuyOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 給人看的單號，例如 GBO-20260510-0001 */
    @Column(nullable = false, unique = true, length = 32)
    private String orderNo;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_buy_id", nullable = false, unique = true)
    private GroupBuy groupBuy;

    /** 團主（即 GroupBuy.host） */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farmer_id", nullable = false)
    private User farmer;

    /** 全團總數量（所有 JOINED 團員 quantity 加總） */
    @Column(nullable = false)
    private Integer totalQuantity;

    /** 全團總金額（所有 JOINED 團員 subtotal 加總） */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GroupBuyOrderStatus status = GroupBuyOrderStatus.PENDING_PAYMENT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.CASH_ON_DELIVERY;

    /** 團主完成付款時間（模擬付款） */
    private LocalDateTime paidAt;

    /** 全部完成（COMPLETED）時間 */
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "groupBuyOrder", fetch = FetchType.LAZY)
    @Builder.Default
    private List<GroupBuyParticipation> participations = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
