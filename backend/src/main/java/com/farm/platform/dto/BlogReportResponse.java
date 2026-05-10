package com.farm.platform.dto;

import com.farm.platform.entity.BlogCommentReport;
import com.farm.platform.entity.BlogReport;
import com.farm.platform.entity.BlogReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class BlogReportResponse {
    private Long id;
    private String targetType;          // BLOG / COMMENT
    private Long targetId;
    private String targetTitleOrContent;
    private Long blogId;                // for COMMENT, the blog it belongs to
    private Long reporterId;
    private String reporterName;
    private String reason;
    private BlogReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;
    private String handlerName;

    public static BlogReportResponse fromBlog(BlogReport r) {
        return BlogReportResponse.builder()
                .id(r.getId())
                .targetType("BLOG")
                .targetId(r.getBlog().getId())
                .targetTitleOrContent(r.getBlog().getTitle())
                .blogId(r.getBlog().getId())
                .reporterId(r.getReporter().getId())
                .reporterName(r.getReporter().getName())
                .reason(r.getReason())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .handledAt(r.getHandledAt())
                .handlerName(r.getAdmin() != null ? r.getAdmin().getName() : null)
                .build();
    }

    public static BlogReportResponse fromComment(BlogCommentReport r) {
        var c = r.getComment();
        return BlogReportResponse.builder()
                .id(r.getId())
                .targetType("COMMENT")
                .targetId(c.getId())
                .targetTitleOrContent(c.getContent())
                .blogId(c.getBlog().getId())
                .reporterId(r.getReporter().getId())
                .reporterName(r.getReporter().getName())
                .reason(r.getReason())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .handledAt(r.getHandledAt())
                .handlerName(r.getAdmin() != null ? r.getAdmin().getName() : null)
                .build();
    }
}
