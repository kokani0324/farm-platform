package com.farm.platform.farmtrip.entity;

import com.farm.platform.account.entity.Member;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** 體驗活動評論 (spec FARM_TRIP_COMMENT) */
@Entity
@Table(name = "farm_trip_comments", indexes = {
        @Index(name = "idx_ftc_trip", columnList = "farm_trip_id"),
        @Index(name = "idx_ftc_user", columnList = "user_id"),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FarmTripComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_trip_id", nullable = false)
    private FarmTrip farmTrip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Member user;

    /** 1~5 */
    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, length = 500)
    private String content;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
