package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 團購參與記錄(每位參團者一筆)。
 * 收件資訊欄位依 spec 拆 zipcode / city / dist / detail；
 * 成團後透過 {@link GroupBuyOrder} 聚合到團主整單，
 * 各團員仍各自追蹤出貨/收貨狀態（{@link #receiptStatus}）。
 */
@Entity
@Table(name = "group_buy_participations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_buy_id", "user_id"}),
        indexes = {
                @Index(name = "idx_gbp_groupbuy", columnList = "group_buy_id"),
                @Index(name = "idx_gbp_user", columnList = "user_id"),
                @Index(name = "idx_gbp_gborder", columnList = "group_buy_order_id"),
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupBuyParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_buy_id", nullable = false)
    private GroupBuy groupBuy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isHost = false;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /* ===== 收件資訊（依 spec 拆四欄） ===== */
    @Column(nullable = false, length = 50)
    private String recipientName;

    @Column(nullable = false, length = 20)
    private String recipientPhone;

    @Column(nullable = false, length = 10)
    private String shippingZipcode;

    @Column(nullable = false, length = 20)
    private String shippingCity;

    @Column(nullable = false, length = 20)
    private String shippingDist;

    @Column(nullable = false, length = 200)
    private String shippingDetail;

    @Column(length = 500)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ParticipationStatus status = ParticipationStatus.JOINED;

    /* ===== 付款追蹤（加入即付款） ===== */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PaymentMethod paymentMethod;

    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;

    /* ===== 出貨 / 收貨追蹤 ===== */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReceiptStatus receiptStatus = ReceiptStatus.NOT_SHIPPED;

    /** 團員確認收貨時間 */
    private LocalDateTime receiptDatetime;

    /** 小農出貨時間 */
    private LocalDateTime shippedAt;

    /** 成團後對應的團購整單（屬團主） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_buy_order_id")
    private GroupBuyOrder groupBuyOrder;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    /** 組合完整地址供顯示 */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (shippingZipcode != null) sb.append(shippingZipcode).append(' ');
        if (shippingCity != null) sb.append(shippingCity);
        if (shippingDist != null) sb.append(shippingDist);
        if (shippingDetail != null) sb.append(shippingDetail);
        return sb.toString();
    }
}
