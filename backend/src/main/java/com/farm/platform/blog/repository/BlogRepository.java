package com.farm.platform.blog.repository;

import com.farm.platform.blog.entity.Blog;
import com.farm.platform.blog.entity.BlogStatus;
import com.farm.platform.account.entity.Farmer;
import com.farm.platform.account.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {

    @Query("select b from Blog b " +
            "left join fetch b.authorMember " +
            "left join fetch b.authorFarmer " +
            "join fetch b.blogType where b.id = :id")
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

    Page<Blog> findByAuthorMemberOrderByCreatedAtDesc(Member author, Pageable pageable);
    Page<Blog> findByAuthorFarmerOrderByCreatedAtDesc(Farmer author, Pageable pageable);

    long countByStatus(BlogStatus status);

    java.util.List<Blog> findByBlogType(com.farm.platform.blog.entity.BlogType blogType);

    boolean existsByAuthorFarmerAndBlogType(Farmer authorFarmer, com.farm.platform.blog.entity.BlogType blogType);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Blog b SET b.blogType = :target WHERE b.blogType = :source")
    int reassignBlogType(@Param("source") com.farm.platform.blog.entity.BlogType source,
                         @Param("target") com.farm.platform.blog.entity.BlogType target);
}
