package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 團購發起請求(消費者向小農發出)。
 * 通過後系統建立 {@link GroupBuy} 並把這張 request 設為 APPROVED。
 */
@Entity
@Table(name = "group_buy_requests", indexes = {
        @Index(name = "idx_gbr_initiator", columnList = "initiator_id"),
        @Index(name = "idx_gbr_farmer", columnList = "farmer_id"),
        @Index(name = "idx_gbr_status", columnList = "status"),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupBuyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** 發起人(消費者) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    /** 該商品的小農(反 3NF,查詢方便) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farmer_id", nullable = false)
    private User farmer;

    @Column(nullable = false)
    private Integer targetQuantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal groupPrice;

    @Column(nullable = false)
    private LocalDateTime openDate;

    @Column(nullable = false)
    private LocalDateTime deadlineDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GroupBuyRequestStatus status = GroupBuyRequestStatus.PENDING;

    /** 拒絕時填寫的原因 */
    @Column(length = 500)
    private String rejectReason;

    /** 給發起者的提案訊息(選填) */
    @Column(length = 500)
    private String message;

    private LocalDateTime repliedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;
}
