package com.farm.platform.repository;

import com.farm.platform.entity.Product;
import com.farm.platform.entity.ProductStatus;
import com.farm.platform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** 小農查詢自己的商品 */
    List<Product> findByFarmerOrderByCreatedAtDesc(User farmer);

    /**
     * 公開瀏覽：依分類 / 關鍵字搜尋 + 分頁
     * categoryId 為 null 時不過濾分類
     * keyword 為 null/空字串時不過濾關鍵字
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.status = :status
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Product> search(@Param("status") ProductStatus status,
                         @Param("categoryId") Long categoryId,
                         @Param("keyword") String keyword,
                         Pageable pageable);

    /** 後台:全部商品列表(含關鍵字過濾) */
    @Query("""
            SELECT p FROM Product p
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.farmer.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY p.createdAt DESC
            """)
    Page<Product> adminSearch(@Param("keyword") String keyword, Pageable pageable);

    long countByStatus(ProductStatus status);
}
