package com.farm.platform.repository;

import com.farm.platform.entity.Blog;
import com.farm.platform.entity.BlogReport;
import com.farm.platform.entity.BlogReportStatus;
import com.farm.platform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogReportRepository extends JpaRepository<BlogReport, Long> {

    Page<BlogReport> findByStatusOrderByCreatedAtDesc(BlogReportStatus status, Pageable pageable);

    Page<BlogReport> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByBlogAndReporter(Blog blog, User reporter);

    long countByStatus(BlogReportStatus status);
}
