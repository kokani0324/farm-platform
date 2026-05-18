package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 部落格文章。Phase B：作者拆成 authorMember / authorFarmer 雙 nullable FK (XOR)，
 * spec BLOG.blog_title 改 60；blog_img 用 URL。
 */
@Entity
@Table(name = "blogs", indexes = {
        @Index(name = "idx_blog_author_member", columnList = "author_member_id"),
        @Index(name = "idx_blog_author_farmer", columnList = "author_farmer_id"),
        @Index(name = "idx_blog_type", columnList = "blog_type_id"),
        @Index(name = "idx_blog_status", columnList = "status"),
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 封面圖 (對應 spec blog_img) */
    @Column(length = 500)
    private String coverImageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Integer likeCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer commentCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BlogStatus status = BlogStatus.PUBLISHED;

    /** XOR：authorMember 與 authorFarmer 必須恰好一個非 null */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_member_id")
    private Member authorMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_farmer_id")
    private Farmer authorFarmer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blog_type_id", nullable = false)
    private BlogType blogType;

    /** 文中介紹的商品：可連結商品頁、消費者可從文章直接加入購物車 */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "blog_products",
            joinColumns = @JoinColumn(name = "blog_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    @Builder.Default
    private List<Product> featuredProducts = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /* === 作者顯示輔助 === */

    public AccountType getAuthorType() {
        if (authorMember != null) return AccountType.MEMBER;
        if (authorFarmer != null) return AccountType.FARMER;
        return null;
    }

    public Long getAuthorId() {
        if (authorMember != null) return authorMember.getId();
        if (authorFarmer != null) return authorFarmer.getId();
        return null;
    }

    public String getAuthorEmail() {
        if (authorMember != null) return authorMember.getEmail();
        if (authorFarmer != null) return authorFarmer.getEmail();
        return null;
    }

    public String getAuthorDisplayName() {
        if (authorMember != null) {
            return authorMember.getNickname() != null && !authorMember.getNickname().isBlank()
                    ? authorMember.getNickname() : authorMember.getName();
        }
        if (authorFarmer != null) {
            return authorFarmer.getFarmName();
        }
        return "（未知）";
    }
}
