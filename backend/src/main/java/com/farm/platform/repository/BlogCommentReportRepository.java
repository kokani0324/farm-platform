package com.farm.platform.repository;

import com.farm.platform.entity.BlogComment;
import com.farm.platform.entity.BlogCommentReport;
import com.farm.platform.entity.BlogReportStatus;
import com.farm.platform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogCommentReportRepository extends JpaRepository<BlogCommentReport, Long> {

    Page<BlogCommentReport> findByStatusOrderByCreatedAtDesc(BlogReportStatus status, Pageable pageable);

    Page<BlogCommentReport> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByCommentAndReporter(BlogComment comment, User reporter);

    long countByStatus(BlogReportStatus status);
}
