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
//@Query("...") 的意思是 不要用你預設的功能猜，請嚴格按照 括號裡寫的 JPQL 語法去查資料庫
    // select b from Blog b 等於SQL 的 select b form Blog as b
    //join fetch b.author join fetch b.blogType ，利用 join 將表格連接起來，並加上 fetch 關鍵字，強制把 b (文章) 裡面的 author 屬性和 blogType 屬性，在這次查詢中立刻撈出真實資料
    //fetch 這個關鍵字的核心功能是：「強制立即抓取（Eager Loading）關聯的資料」。
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
            """) //上面的程式碼是動態的條件搜尋
    //order by b.createdAt desc 是用建立時間由新到舊去排序
    Page<Blog> publicSearch(@Param("status") BlogStatus status,
                            @Param("typeId") Long typeId,
                            @Param("keyword") String keyword,
                            Pageable pageable);

    Page<Blog> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);

    long countByStatus(BlogStatus status);
}
