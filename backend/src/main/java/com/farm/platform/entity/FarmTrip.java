package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 體驗活動(對應 spec FARM_TRIP)
 * spec 欄位:farm_trip_id / user_id / farmer_id / farm_trip_type / farm_trip_title /
 *         farm_trip_pic / farm_trip_intro / farm_trip_start / farm_trip_end /
 *         trip_book_start / trip_book_end
 * 補強: location, price, capacity, currentBookings, status
 */
@Entity
@Table(name = "farm_trips", indexes = {
        @Index(name = "idx_ft_farmer", columnList = "farmer_id"),
        @Index(name = "idx_ft_category", columnList = "category_id"),
        @Index(name = "idx_ft_status", columnList = "status"),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FarmTrip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(length = 500)
    private String intro;

    /** 對應 spec farm_trip_pic;以 URL 取代 LONGBLOB,跟 Product 一致 */
    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripType tripType;

    @Column(length = 100)
    private String location;

    /** 每人報名費 */
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    /** 名額上限 */
    @Column(nullable = false)
    @Builder.Default
    private Integer capacity = 0;

    /** 已預約人數(反3NF,效能用;每次預約/取消同步維護) */
    @Column(nullable = false)
    @Builder.Default
    private Integer currentBookings = 0;

    /** 活動開始/結束時間 */
    @Column(nullable = false)
    private LocalDateTime tripStart;

    @Column(nullable = false)
    private LocalDateTime tripEnd;

    /** 報名開始/截止時間 */
    @Column(nullable = false)
    private LocalDateTime bookStart;

    @Column(nullable = false)
    private LocalDateTime bookEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FarmTripStatus status = FarmTripStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farmer_id", nullable = false)
    private User farmer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private FarmTripCategory category;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public int remainingCapacity() {
        return Math.max(0, capacity - currentBookings);
    }
}
