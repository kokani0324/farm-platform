package com.farm.platform.service;

import com.farm.platform.dto.*;
import com.farm.platform.entity.*;
import com.farm.platform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BlogService {

    private final BlogRepository blogRepo;
    private final BlogTypeRepository typeRepo;
    private final BlogCommentRepository commentRepo;
    private final BlogReportRepository reportRepo;
    private final BlogCommentReportRepository commentReportRepo;
    private final UserRepository userRepo;

    /* ============================ 公開瀏覽 ============================ */

    public List<BlogTypeResponse> listTypes() {
        return typeRepo.findAllByOrderBySortOrderAsc().stream().map(BlogTypeResponse::from).toList();
    }

    public PageResponse<BlogResponse> listPublic(Long typeId, String keyword, Pageable pageable) {
        Page<Blog> page = blogRepo.publicSearch(BlogStatus.PUBLISHED, typeId, keyword, pageable);
        return PageResponse.of(page, BlogResponse::summary);
    }

    @Transactional
    public BlogResponse getDetail(Long id) {
        Blog b = blogRepo.findFullById(id)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        if (b.getStatus() != BlogStatus.PUBLISHED) {
            throw new IllegalStateException("文章已不公開顯示");
        }
        b.setViewCount(b.getViewCount() + 1);
        return BlogResponse.from(b);
    }

    /* ============================ 寫作 ============================ */

    @Transactional
    public BlogResponse create(String email, BlogRequest req) {
        User author = getUser(email);
        BlogType type = typeRepo.findById(req.getBlogTypeId())
                .orElseThrow(() -> new IllegalArgumentException("分類不存在"));
        Blog b = Blog.builder()
                .author(author)
                .blogType(type)
                .title(req.getTitle())
                .content(req.getContent())
                .coverImageUrl(req.getCoverImageUrl())
                .status(BlogStatus.PUBLISHED)
                .build();
        return BlogResponse.from(blogRepo.save(b));
    }

    @Transactional
    public BlogResponse update(String email, Long id, BlogRequest req) {
        Blog b = blogRepo.findFullById(id)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        if (!b.getAuthor().getEmail().equals(email)) throw new AccessDeniedException("無權編輯");
        if (b.getStatus() == BlogStatus.HIDDEN) throw new IllegalStateException("被管理員隱藏的文章無法編輯");
        BlogType type = typeRepo.findById(req.getBlogTypeId())
                .orElseThrow(() -> new IllegalArgumentException("分類不存在"));
        b.setBlogType(type);
        b.setTitle(req.getTitle());
        b.setContent(req.getContent());
        b.setCoverImageUrl(req.getCoverImageUrl());
        return BlogResponse.from(b);
    }

    @Transactional
    public void delete(String email, Long id) {
        Blog b = blogRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        if (!b.getAuthor().getEmail().equals(email)) throw new AccessDeniedException("無權刪除");
        b.setStatus(BlogStatus.DELETED);
    }

    public PageResponse<BlogResponse> myBlogs(String email, Pageable pageable) {
        User me = getUser(email);
        return PageResponse.of(blogRepo.findByAuthorOrderByCreatedAtDesc(me, pageable), BlogResponse::summary);
    }

    /* ============================ 按讚 ============================ */

    @Transactional
    public BlogResponse likeBlog(Long id) {
        Blog b = blogRepo.findFullById(id)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        b.setLikeCount(b.getLikeCount() + 1);
        return BlogResponse.from(b);
    }

    /* ============================ 留言 ============================ */

    public PageResponse<BlogCommentResponse> listComments(Long blogId, Pageable pageable) {
        Blog b = blogRepo.findById(blogId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        return PageResponse.of(commentRepo.findByBlogAndStatusOrderByCreatedAtDesc(b, BlogStatus.PUBLISHED, pageable),
                BlogCommentResponse::from);
    }

    @Transactional
    public BlogCommentResponse addComment(String email, Long blogId, BlogCommentRequest req) {
        User author = getUser(email);
        Blog b = blogRepo.findById(blogId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        if (b.getStatus() != BlogStatus.PUBLISHED) throw new IllegalStateException("此文章已不接受留言");
        BlogComment c = BlogComment.builder()
                .blog(b)
                .author(author)
                .content(req.getContent())
                .status(BlogStatus.PUBLISHED)
                .build();
        commentRepo.save(c);
        b.setCommentCount(b.getCommentCount() + 1);
        return BlogCommentResponse.from(c);
    }

    @Transactional
    public void deleteComment(String email, Long commentId) {
        BlogComment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("留言不存在"));
        if (!c.getAuthor().getEmail().equals(email)) throw new AccessDeniedException("無權刪除");
        if (c.getStatus() == BlogStatus.DELETED) return;
        c.setStatus(BlogStatus.DELETED);
        Blog b = c.getBlog();
        b.setCommentCount(Math.max(0, b.getCommentCount() - 1));
    }

    /* ============================ 檢舉 ============================ */

    @Transactional
    public void reportBlog(String email, Long blogId, BlogReportRequest req) {
        User reporter = getUser(email);
        Blog b = blogRepo.findById(blogId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        if (b.getAuthor().getId().equals(reporter.getId())) {
            throw new IllegalStateException("無法檢舉自己的文章");
        }
        if (reportRepo.existsByBlogAndReporter(b, reporter)) {
            throw new IllegalStateException("您已檢舉過此文章");
        }
        reportRepo.save(BlogReport.builder()
                .blog(b).reporter(reporter).reason(req.getReason())
                .status(BlogReportStatus.PENDING).build());
    }

    @Transactional
    public void reportComment(String email, Long commentId, BlogReportRequest req) {
        User reporter = getUser(email);
        BlogComment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("留言不存在"));
        if (c.getAuthor().getId().equals(reporter.getId())) {
            throw new IllegalStateException("無法檢舉自己的留言");
        }
        if (commentReportRepo.existsByCommentAndReporter(c, reporter)) {
            throw new IllegalStateException("您已檢舉過此留言");
        }
        commentReportRepo.save(BlogCommentReport.builder()
                .comment(c).reporter(reporter).reason(req.getReason())
                .status(BlogReportStatus.PENDING).build());
    }

    /* ============================ Admin ============================ */

    public PageResponse<BlogReportResponse> adminListBlogReports(BlogReportStatus status, Pageable pageable) {
        Page<BlogReport> page = (status != null)
                ? reportRepo.findByStatusOrderByCreatedAtDesc(status, pageable)
                : reportRepo.findAllByOrderByCreatedAtDesc(pageable);
        return PageResponse.of(page, BlogReportResponse::fromBlog);
    }

    public PageResponse<BlogReportResponse> adminListCommentReports(BlogReportStatus status, Pageable pageable) {
        Page<BlogCommentReport> page = (status != null)
                ? commentReportRepo.findByStatusOrderByCreatedAtDesc(status, pageable)
                : commentReportRepo.findAllByOrderByCreatedAtDesc(pageable);
        return PageResponse.of(page, BlogReportResponse::fromComment);
    }

    @Transactional
    public BlogReportResponse adminHandleBlogReport(String adminEmail, Long reportId, HandleReportRequest req) {
        User admin = getUser(adminEmail);
        BlogReport r = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("檢舉不存在"));
        if (r.getStatus() != BlogReportStatus.PENDING) throw new IllegalStateException("已處理");
        if (req.getAction() == BlogReportStatus.HIDDEN) {
            r.getBlog().setStatus(BlogStatus.HIDDEN);
            r.setStatus(BlogReportStatus.HIDDEN);
        } else if (req.getAction() == BlogReportStatus.KEEP) {
            r.setStatus(BlogReportStatus.KEEP);
        } else {
            throw new IllegalArgumentException("動作必須是 KEEP 或 HIDDEN");
        }
        r.setAdmin(admin);
        r.setHandledAt(LocalDateTime.now());
        return BlogReportResponse.fromBlog(r);
    }

    @Transactional
    public BlogReportResponse adminHandleCommentReport(String adminEmail, Long reportId, HandleReportRequest req) {
        User admin = getUser(adminEmail);
        BlogCommentReport r = commentReportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("檢舉不存在"));
        if (r.getStatus() != BlogReportStatus.PENDING) throw new IllegalStateException("已處理");
        if (req.getAction() == BlogReportStatus.HIDDEN) {
            BlogComment c = r.getComment();
            c.setStatus(BlogStatus.HIDDEN);
            c.getBlog().setCommentCount(Math.max(0, c.getBlog().getCommentCount() - 1));
            r.setStatus(BlogReportStatus.HIDDEN);
        } else if (req.getAction() == BlogReportStatus.KEEP) {
            r.setStatus(BlogReportStatus.KEEP);
        } else {
            throw new IllegalArgumentException("動作必須是 KEEP 或 HIDDEN");
        }
        r.setAdmin(admin);
        r.setHandledAt(LocalDateTime.now());
        return BlogReportResponse.fromComment(r);
    }

    public long countPendingReports() {
        return reportRepo.countByStatus(BlogReportStatus.PENDING)
                + commentReportRepo.countByStatus(BlogReportStatus.PENDING);
    }

    public long countBlogs() {
        return blogRepo.countByStatus(BlogStatus.PUBLISHED);
    }

    /* ============================ helpers ============================ */

    private User getUser(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("使用者不存在"));
    }
}
