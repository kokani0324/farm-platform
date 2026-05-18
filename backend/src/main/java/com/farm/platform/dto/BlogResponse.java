package com.farm.platform.dto;

import com.farm.platform.entity.AccountType;
import com.farm.platform.entity.Blog;
import com.farm.platform.entity.BlogStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class BlogResponse {
    private Long id;
    private String title;
    private String content;
    private String coverImageUrl;
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;
    private BlogStatus status;

    private Long authorId;
    private String authorName;
    private AccountType authorType;

    private Long blogTypeId;
    private String blogTypeName;
    private String blogTypeIcon;

    /** 文中介紹的商品（詳情頁顯示，列表頁可能為 null） */
    private List<BlogProductMini> featuredProducts;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BlogResponse from(Blog b) {
        return from(b, true);
    }

    public static BlogResponse from(Blog b, boolean includeProducts) {
        List<BlogProductMini> products = null;
        if (includeProducts && b.getFeaturedProducts() != null) {
            products = b.getFeaturedProducts().stream().map(BlogProductMini::from).toList();
        }
        return BlogResponse.builder()
                .id(b.getId())
                .title(b.getTitle())
                .content(b.getContent())
                .coverImageUrl(b.getCoverImageUrl())
                .likeCount(b.getLikeCount())
                .commentCount(b.getCommentCount())
                .viewCount(b.getViewCount())
                .status(b.getStatus())
                .authorId(b.getAuthorId())
                .authorName(b.getAuthorDisplayName())
                .authorType(b.getAuthorType())
                .blogTypeId(b.getBlogType().getId())
                .blogTypeName(b.getBlogType().getName())
                .blogTypeIcon(b.getBlogType().getIcon())
                .featuredProducts(products)
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }

    public static BlogResponse summary(Blog b) {
        BlogResponse r = from(b, false);
        String c = b.getContent();
        r.setContent(c == null ? "" : c.length() > 100 ? c.substring(0, 100) : c);
        return r;
    }
}
