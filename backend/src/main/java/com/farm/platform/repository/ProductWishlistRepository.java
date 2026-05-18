package com.farm.platform.repository;

import com.farm.platform.entity.Member;
import com.farm.platform.entity.Product;
import com.farm.platform.entity.ProductWishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductWishlistRepository extends JpaRepository<ProductWishlist, Long> {

    boolean existsByMemberAndProduct(Member member, Product product);

    void deleteByMemberAndProduct(Member member, Product product);

    Page<ProductWishlist> findByMemberOrderByCreatedAtDesc(Member member, Pageable pageable);

    long countByMember(Member member);
}
