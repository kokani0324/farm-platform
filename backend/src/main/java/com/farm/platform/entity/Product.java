package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_farmer", columnList = "farmer_id"),
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_status", columnList = "status"),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** 單價（每單位） */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** 計價單位，如「斤」「箱」「公斤」 */
    @Column(nullable = false, length = 20)
    private String unit;

    /** 庫存數量 */
    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    /** 圖片網址（先支援單張，URL 形式） */
    @Column(length = 500)
    private String imageUrl;

    /** 產地 */
    @Column(length = 100)
    private String origin;

    /** 出貨方式：黑貓/全家店到店/自取 */
    @Column(length = 50)
    private String shippingMethod;

    /** 是否開放團購 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean groupBuyEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farmer_id", nullable = false)
    private User farmer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
