package com.farm.platform.dto;

import com.farm.platform.entity.News;
import com.farm.platform.entity.NewsStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class NewsResponse {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverImageUrl;
    private NewsStatus status;
    private String publishedBy;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NewsResponse from(News n) {
        return NewsResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .summary(n.getSummary())
                .content(n.getContent())
                .coverImageUrl(n.getCoverImageUrl())
                .status(n.getStatus())
                .publishedBy(n.getAdmin() != null ? n.getAdmin().getName() : null)
                .publishedAt(n.getPublishedAt())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }

    public static NewsResponse summary(News n) {
        NewsResponse r = from(n);
        if (r.summary == null && n.getContent() != null) {
            String c = n.getContent();
            r.summary = c.length() > 80 ? c.substring(0, 80) : c;
        }
        r.content = null;
        return r;
    }
}
