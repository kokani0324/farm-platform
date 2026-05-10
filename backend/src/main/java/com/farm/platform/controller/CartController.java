package com.farm.platform.controller;

import com.farm.platform.dto.AddToCartRequest;
import com.farm.platform.dto.CartDto;
import com.farm.platform.dto.UpdateCartItemRequest;
import com.farm.platform.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartDto getCart(@AuthenticationPrincipal UserDetails me) {
        return cartService.getCart(me.getUsername());
    }

    @PostMapping("/items")
    public CartDto addItem(@AuthenticationPrincipal UserDetails me,
                           @Valid @RequestBody AddToCartRequest req) {
        return cartService.addItem(me.getUsername(), req.getProductId(), req.getQuantity());
    }

    @PutMapping("/items/{productId}")
    public CartDto updateQuantity(@AuthenticationPrincipal UserDetails me,
                                  @PathVariable Long productId,
                                  @Valid @RequestBody UpdateCartItemRequest req) {
        return cartService.updateQuantity(me.getUsername(), productId, req.getQuantity());
    }

    @DeleteMapping("/items/{productId}")
    public CartDto removeItem(@AuthenticationPrincipal UserDetails me,
                              @PathVariable Long productId) {
        return cartService.removeItem(me.getUsername(), productId);
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(@AuthenticationPrincipal UserDetails me) {
        cartService.clear(me.getUsername());
        return ResponseEntity.noContent().build();
    }
}
