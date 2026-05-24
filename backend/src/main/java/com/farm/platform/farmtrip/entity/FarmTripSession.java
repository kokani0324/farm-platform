package com.farm.platform.farmtrip.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 體驗活動場次 (spec FARM_TRIP_SESSION)
 * 每天一個場次，PER_WEIGHT 模式無人數上限、僅做容量佔位。
 */
@Entity
@Table(name = "farm_trip_sessions", indexes = {
        @Index(name = "idx_fts_trip", columnList = "farm_trip_id"),
        @Index(name = "idx_fts_status", columnList = "status"),
        @Index(name = "idx_fts_trip_start", columnList = "trip_start"),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FarmTripSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_trip_id", nullable = false)
    private FarmTrip farmTrip;

    /** 本場次價格（PER_PERSON：每人；PER_WEIGHT：每公斤） */
    @Column(name = "session_price", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal sessionPrice = BigDecimal.ZERO;

    @Column(name = "trip_start", nullable = false)
    private LocalDateTime tripStart;

    @Column(name = "trip_end", nullable = false)
    private LocalDateTime tripEnd;

    @Column(name = "book_start", nullable = false)
    private LocalDateTime bookStart;

    /** 報名截止；spec 範例：活動當天前 3 天 */
    @Column(name = "book_end", nullable = false)
    private LocalDateTime bookEnd;

    /** 目前已預約人數（PER_WEIGHT 也記錄人數，僅用於統計顯示） */
    @Column(nullable = false)
    @Builder.Default
    private Integer attendance = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FarmTripSessionStatus status = FarmTripSessionStatus.ACTIVE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
