package com.farm.platform.dto;

import com.farm.platform.entity.Blog;
import com.farm.platform.entity.BlogStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

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

    private Long blogTypeId;
    private String blogTypeName;
    private String blogTypeIcon;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BlogResponse from(Blog b) {
        return BlogResponse.builder()
                .id(b.getId())
                .title(b.getTitle())
                .content(b.getContent())
                .coverImageUrl(b.getCoverImageUrl())
                .likeCount(b.getLikeCount())
                .commentCount(b.getCommentCount())
                .viewCount(b.getViewCount())
                .status(b.getStatus())
                .authorId(b.getAuthor().getId())
                .authorName(b.getAuthor().getName())
                .blogTypeId(b.getBlogType().getId())
                .blogTypeName(b.getBlogType().getName())
                .blogTypeIcon(b.getBlogType().getIcon())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }

    /** 列表用簡略版(不帶 content) */
    public static BlogResponse summary(Blog b) {
        BlogResponse r = from(b);
        String c = b.getContent();
        // 截取前 100 字當摘要
        r.setContent(c == null ? "" : c.length() > 100 ? c.substring(0, 100) : c);
        return r;
    }
}
