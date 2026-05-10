package com.farm.platform.controller;

import com.farm.platform.dto.CheckoutRequest;
import com.farm.platform.dto.OrderResponse;
import com.farm.platform.dto.PageResponse;
import com.farm.platform.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** 結帳：消費者按下「確認下單」 → 從 Redis 購物車自動拆單 */
    @PostMapping("/checkout")
    public List<OrderResponse> checkout(@AuthenticationPrincipal UserDetails me,
                                        @Valid @RequestBody CheckoutRequest req) {
        return orderService.checkout(me.getUsername(), req);
    }

    /** 我的訂單（消費者） */
    @GetMapping
    public PageResponse<OrderResponse> myOrders(@AuthenticationPrincipal UserDetails me,
                                                @PageableDefault(size = 10) Pageable pageable) {
        return orderService.myOrders(me.getUsername(), pageable);
    }

    /** 小農接到的訂單 */
    @GetMapping("/farmer")
    public PageResponse<OrderResponse> farmerOrders(@AuthenticationPrincipal UserDetails me,
                                                    @PageableDefault(size = 10) Pageable pageable) {
        return orderService.farmerOrders(me.getUsername(), pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse getOne(@AuthenticationPrincipal UserDetails me,
                                @PathVariable Long id) {
        return orderService.getDetail(me.getUsername(), id);
    }

    /** 模擬付款 */
    @PostMapping("/{id}/pay")
    public OrderResponse pay(@AuthenticationPrincipal UserDetails me,
                             @PathVariable Long id) {
        return orderService.pay(me.getUsername(), id);
    }

    /** 取消未付款訂單（會還原庫存） */
    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal UserDetails me,
                                @PathVariable Long id) {
        return orderService.cancel(me.getUsername(), id);
    }

    /** 小農：標記為已出貨 */
    @PostMapping("/{id}/ship")
    public OrderResponse ship(@AuthenticationPrincipal UserDetails me,
                              @PathVariable Long id) {
        return orderService.markShipped(me.getUsername(), id);
    }

    /** 消費者：確認收貨 → COMPLETED */
    @PostMapping("/{id}/confirm")
    public OrderResponse confirmReceipt(@AuthenticationPrincipal UserDetails me,
                                        @PathVariable Long id) {
        return orderService.confirmReceipt(me.getUsername(), id);
    }
}
