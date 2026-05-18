package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 會員商品收藏 (spec GENERAL_MEMBER_PRODUCT_WISHLIST)
 * 一個會員 × 一個商品 至多一筆。
 */
@Entity
@Table(name = "product_wishlists",
        uniqueConstraints = @UniqueConstraint(name = "uk_wl_member_product", columnNames = {"member_id", "product_id"}),
        indexes = {
                @Index(name = "idx_wl_member", columnList = "member_id"),
                @Index(name = "idx_wl_product", columnList = "product_id"),
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductWishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
