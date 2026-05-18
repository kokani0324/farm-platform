package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 體驗活動 (spec FARM_TRIP)
 *
 * spec 對照：
 *   farm_trip_id / farmer_id / farm_trip_type / farm_trip_title /
 *   farm_trip_pic (此處改用 imageUrl) / farm_trip_intro / location /
 *   price (當前展示價，場次另有 sessionPrice) / status / 評論統計
 *
 * 場次 / 預約 / 評論 / 審核 拆到對應子表。
 */
@Entity
@Table(name = "farm_trips", indexes = {
        @Index(name = "idx_ft_farmer", columnList = "farmer_id"),
        @Index(name = "idx_ft_status", columnList = "status"),
        @Index(name = "idx_ft_type", columnList = "trip_type"),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FarmTrip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_type", nullable = false, length = 20)
    private TripType tripType;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(length = 500)
    private String intro;

    /** spec 是 LONGBLOB，這裡與 Product 一致用 URL 字串。 */
    @Column(length = 500)
    private String imageUrl;

    @Column(length = 100)
    private String location;

    /** 計價模式：每人 / 按採收重量 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PricingMode pricingMode = PricingMode.PER_PERSON;

    /** 當前展示價：PER_PERSON 為每人單價，PER_WEIGHT 為每公斤單價 */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    /**
     * 每場次容量上限：
     *  - PER_PERSON 必填（>0）
     *  - PER_WEIGHT 為 null，表示無上限（場次純佔位）
     */
    private Integer capacityPerSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FarmTripStatus status = FarmTripStatus.PENDING;

    /** 評論總人數（spec 第 15 欄） */
    @Column(nullable = false)
    @Builder.Default
    private Integer ratingCount = 0;

    /** 評論星星總數（spec 第 16 欄，前端算平均 = total / count） */
    @Column(nullable = false)
    @Builder.Default
    private Integer ratingTotalStars = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public boolean isPerWeight() { return pricingMode == PricingMode.PER_WEIGHT; }
}
