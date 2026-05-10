package com.farm.platform.repository;

import com.farm.platform.entity.Blog;
import com.farm.platform.entity.BlogStatus;
import com.farm.platform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {

    @Query("select b from Blog b join fetch b.author join fetch b.blogType where b.id = :id")
    Optional<Blog> findFullById(@Param("id") Long id);

    @Query("""
            select b from Blog b
            where b.status = :status
              and (:typeId is null or b.blogType.id = :typeId)
              and (:keyword is null or :keyword = ''
                   or lower(b.title) like lower(concat('%', :keyword, '%'))
                   or lower(b.content) like lower(concat('%', :keyword, '%')))
            order by b.createdAt desc
            """)
    Page<Blog> publicSearch(@Param("status") BlogStatus status,
                            @Param("typeId") Long typeId,
                            @Param("keyword") String keyword,
                            Pageable pageable);

    Page<Blog> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);

    long countByStatus(BlogStatus status);
}
