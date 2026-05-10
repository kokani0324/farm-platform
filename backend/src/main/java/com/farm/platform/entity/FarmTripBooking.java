package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 體驗活動預約訂單 */
@Entity
@Table(name = "farm_trip_bookings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ftb_no", columnNames = "booking_no"),
        },
        indexes = {
                @Index(name = "idx_ftb_trip", columnList = "farm_trip_id"),
                @Index(name = "idx_ftb_user", columnList = "user_id"),
                @Index(name = "idx_ftb_status", columnList = "status"),
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FarmTripBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_no", nullable = false, length = 30)
    private String bookingNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_trip_id", nullable = false)
    private FarmTrip farmTrip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer numPeople;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 50)
    private String contactName;

    @Column(nullable = false, length = 20)
    private String contactPhone;

    @Column(length = 500)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FarmTripBookingStatus status = FarmTripBookingStatus.CONFIRMED;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime bookedAt;

    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;
}
