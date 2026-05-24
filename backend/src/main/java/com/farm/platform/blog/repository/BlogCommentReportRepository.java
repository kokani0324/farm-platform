package com.farm.platform.blog.repository;

import com.farm.platform.blog.entity.BlogComment;
import com.farm.platform.blog.entity.BlogCommentReport;
import com.farm.platform.blog.entity.BlogReportStatus;
import com.farm.platform.account.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogCommentReportRepository extends JpaRepository<BlogCommentReport, Long> {

    Page<BlogCommentReport> findByStatusOrderByCreatedAtDesc(BlogReportStatus status, Pageable pageable);

    Page<BlogCommentReport> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByCommentAndReporter(BlogComment comment, Member reporter);

    long countByStatus(BlogReportStatus status);
}
