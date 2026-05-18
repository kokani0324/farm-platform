package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/** 體驗活動審核紀錄 (spec FARM_TRIP_AUDITS) */
@Entity
@Table(name = "farm_trip_audits", indexes = {
        @Index(name = "idx_fta_trip", columnList = "farm_trip_id"),
        @Index(name = "idx_fta_admin", columnList = "admin_id"),
        @Index(name = "idx_fta_status", columnList = "status"),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FarmTripAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_trip_id", nullable = false)
    private FarmTrip farmTrip;

    /** 審核人；PENDING 時為 null */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FarmTripAuditStatus status = FarmTripAuditStatus.PENDING;

    @Column(length = 200)
    private String reason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
