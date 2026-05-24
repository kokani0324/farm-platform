package com.farm.platform.blog.repository;

import com.farm.platform.blog.entity.Blog;
import com.farm.platform.blog.entity.BlogComment;
import com.farm.platform.blog.entity.BlogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {

    Page<BlogComment> findByBlogAndStatusOrderByCreatedAtDesc(Blog blog, BlogStatus status, Pageable pageable);

    long countByBlogAndStatus(Blog blog, BlogStatus status);
}
