package com.farm.platform.service;

import com.farm.platform.dto.PageResponse;
import com.farm.platform.dto.WishlistItemResponse;
import com.farm.platform.entity.Member;
import com.farm.platform.entity.Product;
import com.farm.platform.entity.ProductStatus;
import com.farm.platform.entity.ProductWishlist;
import com.farm.platform.repository.MemberRepository;
import com.farm.platform.repository.ProductRepository;
import com.farm.platform.repository.ProductWishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final ProductWishlistRepository wishlistRepo;
    private final MemberRepository memberRepo;
    private final ProductRepository productRepo;

    public PageResponse<WishlistItemResponse> myWishlist(String email, Pageable pageable) {
        Member m = getMember(email);
        return PageResponse.of(wishlistRepo.findByMemberOrderByCreatedAtDesc(m, pageable), WishlistItemResponse::from);
    }

    public boolean check(String email, Long productId) {
        Member m = getMember(email);
        Product p = productRepo.findById(productId).orElse(null);
        if (p == null) return false;
        return wishlistRepo.existsByMemberAndProduct(m, p);
    }

    public long count(String email) {
        return wishlistRepo.countByMember(getMember(email));
    }

    @Transactional
    public void add(String email, Long productId) {
        Member m = getMember(email);
        Product p = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        if (p.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalArgumentException("此商品目前未上架");
        }
        if (wishlistRepo.existsByMemberAndProduct(m, p)) return; // 已收藏視為成功
        wishlistRepo.save(ProductWishlist.builder().member(m).product(p).build());
    }

    @Transactional
    public void remove(String email, Long productId) {
        Member m = getMember(email);
        Product p = productRepo.findById(productId).orElse(null);
        if (p == null) return;
        wishlistRepo.deleteByMemberAndProduct(m, p);
    }

    private Member getMember(String email) {
        return memberRepo.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("僅會員可使用收藏"));
    }
}
