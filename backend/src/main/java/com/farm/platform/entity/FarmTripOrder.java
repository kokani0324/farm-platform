package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 體驗活動預約訂單 (spec FARM_TRIP_ORDER)
 * FK 改成綁場次 (farm_trip_session)，不再綁活動本體。
 */
@Entity
@Table(name = "farm_trip_orders",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_fto_no", columnNames = "booking_no"),
        },
        indexes = {
                @Index(name = "idx_fto_session", columnList = "farm_session_id"),
                @Index(name = "idx_fto_user", columnList = "user_id"),
                @Index(name = "idx_fto_status", columnList = "status"),
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FarmTripOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 使用者可見的訂單編號（spec farm_trip_order_booking_no） */
    @Column(name = "booking_no", nullable = false, length = 30)
    private String bookingNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_session_id", nullable = false)
    private FarmTripSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Member user;

    @Column(nullable = false)
    private Integer numPeople;

    /** 下單當下單價快照（場次價，後續改價不影響歷史單） */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * 總金額：
     *  - PER_PERSON：num_people × unit_price，下單即定
     *  - PER_WEIGHT：下單時為 0；活動結束由小農補登 actual_weight 後 = actual_weight × unit_price
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /** PER_WEIGHT 模式專用：實際採收重量（公斤） */
    @Column(name = "actual_weight", precision = 10, scale = 2)
    private BigDecimal actualWeight;

    @Column(nullable = false, length = 50)
    private String contactName;

    @Column(nullable = false, length = 20)
    private String contactPhone;

    @Column(length = 500)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FarmTripOrderStatus status = FarmTripOrderStatus.CONFIRMED;

    @CreationTimestamp
    @Column(name = "booked_at", nullable = false, updatable = false)
    private LocalDateTime bookedAt;

    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;
}
