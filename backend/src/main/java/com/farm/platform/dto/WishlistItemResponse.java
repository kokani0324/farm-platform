package com.farm.platform.dto;

import com.farm.platform.entity.ProductStatus;
import com.farm.platform.entity.ProductWishlist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class WishlistItemResponse {
    private Long wishlistId;
    private Long productId;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private String unit;
    private Integer stock;
    private String farmerName;
    private String categoryName;
    private Boolean available;
    private LocalDateTime createdAt;

    public static WishlistItemResponse from(ProductWishlist w) {
        var p = w.getProduct();
        return WishlistItemResponse.builder()
                .wishlistId(w.getId())
                .productId(p.getId())
                .name(p.getName())
                .imageUrl(p.getImageUrl())
                .price(p.getPrice())
                .unit(p.getUnit())
                .stock(p.getStock())
                .farmerName(p.getFarmer() != null ? p.getFarmer().getFarmName() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .available(p.getStatus() == ProductStatus.ACTIVE && p.getStock() > 0)
                .createdAt(w.getCreatedAt())
                .build();
    }
}
