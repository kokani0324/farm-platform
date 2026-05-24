package com.farm.platform.shop.controller;

import com.farm.platform.common.dto.PageResponse;
import com.farm.platform.shop.dto.WishlistItemResponse;
import com.farm.platform.shop.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService service;

    @GetMapping
    public PageResponse<WishlistItemResponse> myList(@AuthenticationPrincipal UserDetails me,
                                                     @PageableDefault(size = 24) Pageable pageable) {
        return service.myWishlist(me.getUsername(), pageable);
    }

    @GetMapping("/count")
    public Map<String, Long> count(@AuthenticationPrincipal UserDetails me) {
        return Map.of("count", service.count(me.getUsername()));
    }

    @GetMapping("/products/{productId}/check")
    public Map<String, Boolean> check(@AuthenticationPrincipal UserDetails me, @PathVariable Long productId) {
        return Map.of("inWishlist", service.check(me.getUsername(), productId));
    }

    @PostMapping("/products/{productId}")
    public ResponseEntity<Void> add(@AuthenticationPrincipal UserDetails me, @PathVariable Long productId) {
        service.add(me.getUsername(), productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal UserDetails me, @PathVariable Long productId) {
        service.remove(me.getUsername(), productId);
        return ResponseEntity.noContent().build();
    }
}
