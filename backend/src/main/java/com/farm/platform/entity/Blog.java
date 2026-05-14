package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 部落格文章。spec BLOG.blog_title VARCHAR(15) 太短(連「介紹某某蔬果」都裝不下),
 * 改 60 比較實用;blog_img 用 URL 取代 LONGBLOB;blog_photo (內文圖片) 用 content 中的 markdown/html URL 表達,不單獨建表。
 */
@Entity //這是一個需要跟資料庫連動的實體類別
@Table(name = "blogs", indexes = { //下面是建立索引，幫助加快搜索
        @Index(name = "idx_blog_author", columnList = "author_id"),
        @Index(name = "idx_blog_type", columnList = "blog_type_id"),
        @Index(name = "idx_blog_status", columnList = "status"),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // id的值是由資料庫自動去生成的
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blog_type_id", nullable = false)
    private BlogType blogType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
