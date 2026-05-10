package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 團購參與記錄(每位參團者一筆)。
 * 包含收件資訊,成團後直接用來建立 {@link Order}。
 */
@Entity
@Table(name = "group_buy_participations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_buy_id", "user_id"}),
        indexes = {
                @Index(name = "idx_gbp_groupbuy", columnList = "group_buy_id"),
                @Index(name = "idx_gbp_user", columnList = "user_id"),
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

    /* ===== 收件資訊 ===== */
    @Column(nullable = false, length = 50)
    private String recipientName;

    @Column(nullable = false, length = 20)
    private String recipientPhone;

    @Column(nullable = false, length = 200)
    private String shippingAddress;

    @Column(length = 500)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ParticipationStatus status = ParticipationStatus.JOINED;

    /** 成團後對應的訂單(若已成團) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;
}
