package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 已通過審核的團購活動。所有人都看得到、可加入。
 */
@Entity
@Table(name = "group_buys", indexes = {
        @Index(name = "idx_gb_product", columnList = "product_id"),
        @Index(name = "idx_gb_farmer", columnList = "farmer_id"),
        @Index(name = "idx_gb_host", columnList = "host_id"),
        @Index(name = "idx_gb_status", columnList = "status"),
        @Index(name = "idx_gb_deadline", columnList = "deadline_date"),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupBuy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 來源請求(1:1) */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    private GroupBuyRequest request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** 團主(=發起人) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

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
    private GroupBuyStatus status = GroupBuyStatus.OPEN;

    @OneToMany(mappedBy = "groupBuy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroupBuyParticipation> participations = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
