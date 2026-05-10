package com.farm.platform.dto;

import com.farm.platform.entity.BlogComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class BlogCommentResponse {
    private Long id;
    private Long blogId;
    private Long authorId;
    private String authorName;
    private String content;
    private Integer likeCount;
    private LocalDateTime createdAt;

    public static BlogCommentResponse from(BlogComment c) {
        return BlogCommentResponse.builder()
                .id(c.getId())
                .blogId(c.getBlog().getId())
                .authorId(c.getAuthor().getId())
                .authorName(c.getAuthor().getName())
                .content(c.getContent())
                .likeCount(c.getLikeCount())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
