package com.farm.platform.repository;

import com.farm.platform.entity.Blog;
import com.farm.platform.entity.BlogComment;
import com.farm.platform.entity.BlogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {

    Page<BlogComment> findByBlogAndStatusOrderByCreatedAtDesc(Blog blog, BlogStatus status, Pageable pageable);

    long countByBlogAndStatus(Blog blog, BlogStatus status);
}
