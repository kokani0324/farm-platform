package com.farm.platform.blog.controller;

import com.farm.platform.blog.dto.BlogCommentRequest;
import com.farm.platform.blog.dto.BlogCommentResponse;
import com.farm.platform.blog.dto.BlogReportRequest;
import com.farm.platform.blog.dto.BlogRequest;
import com.farm.platform.blog.dto.BlogResponse;
import com.farm.platform.blog.dto.BlogTypeResponse;
import com.farm.platform.common.dto.PageResponse;
import com.farm.platform.security.AccountPrincipal;
import com.farm.platform.blog.service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService service;

    /* ===== 公開 ===== */

    @GetMapping("/types")
    public List<BlogTypeResponse> types() {
        return service.listTypes();
    }

    @GetMapping
    public PageResponse<BlogResponse> list(@RequestParam(required = false) Long typeId,
                                           @RequestParam(required = false) Long blogTypeId,
                                           @RequestParam(required = false) String keyword,
                                           @PageableDefault(size = 12) Pageable pageable) {
        Long effectiveTypeId = typeId != null ? typeId : blogTypeId;
        return service.listPublic(effectiveTypeId, keyword, pageable);
    }

    @GetMapping("/{id}")
    public BlogResponse detail(@PathVariable Long id) {
        return service.getDetail(id);
    }

    @GetMapping("/{id}/comments")
    public PageResponse<BlogCommentResponse> comments(@PathVariable Long id,
                                                      @PageableDefault(size = 20) Pageable pageable) {
        return service.listComments(id, pageable);
    }

    /* ===== 寫作(會員) ===== */

    @PostMapping
    public BlogResponse create(@AuthenticationPrincipal AccountPrincipal me,
                               @Valid @RequestBody BlogRequest req) {
        return service.create(me, req);
    }

    @PutMapping("/{id}")
    public BlogResponse update(@AuthenticationPrincipal AccountPrincipal me,
                               @PathVariable Long id,
                               @Valid @RequestBody BlogRequest req) {
        return service.update(me, id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AccountPrincipal me, @PathVariable Long id) {
        service.delete(me, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    public PageResponse<BlogResponse> mine(@AuthenticationPrincipal AccountPrincipal me,
                                           @PageableDefault(size = 10) Pageable pageable) {
        return service.myBlogs(me, pageable);
    }

    /* ===== 互動 ===== */

    @PostMapping("/{id}/like")
    public BlogResponse like(@PathVariable Long id) {
        return service.likeBlog(id);
    }

    @PostMapping("/{id}/comments")
    public BlogCommentResponse addComment(@AuthenticationPrincipal UserDetails me,
                                          @PathVariable Long id,
                                          @Valid @RequestBody BlogCommentRequest req) {
        return service.addComment(me.getUsername(), id, req);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@AuthenticationPrincipal UserDetails me,
                                              @PathVariable Long commentId) {
        service.deleteComment(me.getUsername(), commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/report")
    public ResponseEntity<Void> reportBlog(@AuthenticationPrincipal UserDetails me,
                                           @PathVariable Long id,
                                           @Valid @RequestBody BlogReportRequest req) {
        service.reportBlog(me.getUsername(), id, req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments/{commentId}/report")
    public ResponseEntity<Void> reportComment(@AuthenticationPrincipal UserDetails me,
                                              @PathVariable Long commentId,
                                              @Valid @RequestBody BlogReportRequest req) {
        service.reportComment(me.getUsername(), commentId, req);
        return ResponseEntity.noContent().build();
    }
}
