package com.farm.platform.blog.service;

import com.farm.platform.blog.dto.BlogCommentRequest;
import com.farm.platform.blog.dto.BlogCommentResponse;
import com.farm.platform.blog.dto.BlogReportRequest;
import com.farm.platform.blog.dto.BlogReportResponse;
import com.farm.platform.blog.dto.BlogRequest;
import com.farm.platform.blog.dto.BlogResponse;
import com.farm.platform.blog.dto.BlogTypeResponse;
import com.farm.platform.blog.dto.HandleReportRequest;
import com.farm.platform.common.dto.PageResponse;
import com.farm.platform.account.entity.AccountType;
import com.farm.platform.account.entity.Admin;
import com.farm.platform.account.entity.Farmer;
import com.farm.platform.account.entity.Member;
import com.farm.platform.blog.entity.Blog;
import com.farm.platform.blog.entity.BlogComment;
import com.farm.platform.blog.entity.BlogCommentReport;
import com.farm.platform.blog.entity.BlogReport;
import com.farm.platform.blog.entity.BlogReportStatus;
import com.farm.platform.blog.entity.BlogStatus;
import com.farm.platform.blog.entity.BlogType;
import com.farm.platform.shop.entity.Product;
import com.farm.platform.account.repository.AdminRepository;
import com.farm.platform.account.repository.FarmerRepository;
import com.farm.platform.account.repository.MemberRepository;
import com.farm.platform.blog.repository.BlogCommentReportRepository;
import com.farm.platform.blog.repository.BlogCommentRepository;
import com.farm.platform.blog.repository.BlogReportRepository;
import com.farm.platform.blog.repository.BlogRepository;
import com.farm.platform.blog.repository.BlogTypeRepository;
import com.farm.platform.shop.repository.ProductRepository;
import com.farm.platform.security.AccountPrincipal;
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
    private final MemberRepository memberRepo;
    private final FarmerRepository farmerRepo;
    private final AdminRepository adminRepo;
    private final ProductRepository productRepo;

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
    public BlogResponse create(AccountPrincipal me, BlogRequest req) {
        BlogType type = typeRepo.findById(req.getBlogTypeId())
                .orElseThrow(() -> new IllegalArgumentException("分類不存在"));
        checkTypePermission(type, me);
        Blog.BlogBuilder builder = Blog.builder()
                .blogType(type)
                .title(req.getTitle())
                .content(req.getContent())
                .coverImageUrl(req.getCoverImageUrl())
                .status(BlogStatus.PUBLISHED);
        if (me.getType() == AccountType.MEMBER) {
            builder.authorMember(memberRepo.findById(me.getId())
                    .orElseThrow(() -> new AccessDeniedException("會員不存在")));
        } else if (me.getType() == AccountType.FARMER) {
            builder.authorFarmer(farmerRepo.findById(me.getId())
                    .orElseThrow(() -> new AccessDeniedException("小農不存在")));
        } else {
            throw new AccessDeniedException("管理員不能發文");
        }
        Blog saved = blogRepo.save(builder.build());
        setFeaturedProducts(saved, req.getProductIds(), me);
        return BlogResponse.from(saved);
    }

    @Transactional
    public BlogResponse update(AccountPrincipal me, Long id, BlogRequest req) {
        Blog b = blogRepo.findFullById(id)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        checkOwnership(b, me);
        if (b.getStatus() == BlogStatus.HIDDEN) throw new IllegalStateException("被管理員隱藏的文章無法編輯");
        BlogType type = typeRepo.findById(req.getBlogTypeId())
                .orElseThrow(() -> new IllegalArgumentException("分類不存在"));
        checkTypePermission(type, me);
        b.setBlogType(type);
        b.setTitle(req.getTitle());
        b.setContent(req.getContent());
        b.setCoverImageUrl(req.getCoverImageUrl());
        setFeaturedProducts(b, req.getProductIds(), me);
        return BlogResponse.from(b);
    }

    private void checkTypePermission(BlogType type, AccountPrincipal me) {
        boolean isFarmerOnly = Boolean.TRUE.equals(type.getFarmerOnly());
        if (me.getType() == AccountType.FARMER && !isFarmerOnly) {
            throw new AccessDeniedException("小農只能在「產地日記」類別發表文章");
        }
        if (me.getType() == AccountType.MEMBER && isFarmerOnly) {
            throw new AccessDeniedException("「" + type.getName() + "」僅限小農發表");
        }
    }

    private void setFeaturedProducts(Blog blog, List<Long> productIds, AccountPrincipal me) {
        if (productIds == null) return;
        List<Product> products = productIds.isEmpty()
                ? new java.util.ArrayList<>()
                : productRepo.findAllById(productIds);
        // 小農端只能介紹自己的商品（避免幫他人廣告）；會員端可自由介紹任意上架商品。
        if (me.getType() == AccountType.FARMER) {
            for (Product p : products) {
                if (p.getFarmer() == null || !p.getFarmer().getId().equals(me.getId())) {
                    throw new AccessDeniedException("只能介紹自己上架的商品");
                }
            }
        }
        blog.getFeaturedProducts().clear();
        blog.getFeaturedProducts().addAll(products);
    }

    @Transactional
    public void delete(AccountPrincipal me, Long id) {
        Blog b = blogRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        checkOwnership(b, me);
        b.setStatus(BlogStatus.DELETED);
    }

    public PageResponse<BlogResponse> myBlogs(AccountPrincipal me, Pageable pageable) {
        Page<Blog> page;
        if (me.getType() == AccountType.MEMBER) {
            Member m = memberRepo.findById(me.getId())
                    .orElseThrow(() -> new AccessDeniedException("會員不存在"));
            page = blogRepo.findByAuthorMemberOrderByCreatedAtDesc(m, pageable);
        } else if (me.getType() == AccountType.FARMER) {
            Farmer f = farmerRepo.findById(me.getId())
                    .orElseThrow(() -> new AccessDeniedException("小農不存在"));
            page = blogRepo.findByAuthorFarmerOrderByCreatedAtDesc(f, pageable);
        } else {
            throw new AccessDeniedException("管理員無文章");
        }
        return PageResponse.of(page, BlogResponse::summary);
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
        Member author = getMember(email);
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
        Member reporter = getMember(email);
        Blog b = blogRepo.findById(blogId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        if (b.getAuthorType() == AccountType.MEMBER
                && b.getAuthorId() != null && b.getAuthorId().equals(reporter.getId())) {
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
        Member reporter = getMember(email);
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
        Admin admin = getAdmin(adminEmail);
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
        Admin admin = getAdmin(adminEmail);
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

    private Member getMember(String email) {
        return memberRepo.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("會員帳號不存在"));
    }

    private Admin getAdmin(String email) {
        return adminRepo.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("管理員不存在"));
    }

    private void checkOwnership(Blog b, AccountPrincipal me) {
        if (b.getAuthorType() == null || b.getAuthorType() != me.getType()
                || b.getAuthorId() == null || !b.getAuthorId().equals(me.getId())) {
            throw new AccessDeniedException("無權編輯此文章");
        }
    }
}
