package com.farm.platform.controller;

import com.farm.platform.dto.BlogReportResponse;
import com.farm.platform.dto.HandleReportRequest;
import com.farm.platform.dto.PageResponse;
import com.farm.platform.entity.BlogReportStatus;
import com.farm.platform.service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminBlogController {

    private final BlogService service;

    @GetMapping("/blog-reports")
    public PageResponse<BlogReportResponse> listBlogReports(@RequestParam(required = false) BlogReportStatus status,
                                                            @PageableDefault(size = 15) Pageable pageable) {
        return service.adminListBlogReports(status, pageable);
    }

    @PostMapping("/blog-reports/{id}/handle")
    public BlogReportResponse handleBlogReport(@AuthenticationPrincipal UserDetails me,
                                               @PathVariable Long id,
                                               @Valid @RequestBody HandleReportRequest req) {
        return service.adminHandleBlogReport(me.getUsername(), id, req);
    }

    @GetMapping("/blog-comment-reports")
    public PageResponse<BlogReportResponse> listCommentReports(@RequestParam(required = false) BlogReportStatus status,
                                                               @PageableDefault(size = 15) Pageable pageable) {
        return service.adminListCommentReports(status, pageable);
    }

    @PostMapping("/blog-comment-reports/{id}/handle")
    public BlogReportResponse handleCommentReport(@AuthenticationPrincipal UserDetails me,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody HandleReportRequest req) {
        return service.adminHandleCommentReport(me.getUsername(), id, req);
    }
}
