package com.farm.platform.dto;

import com.farm.platform.entity.Product;
import com.farm.platform.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String unit;
    private Integer stock;
    private String imageUrl;
    private String origin;
    private String shippingMethod;
    private Boolean groupBuyEnabled;
    private ProductStatus status;

    private Long categoryId;
    private String categoryName;

    private Long farmerId;
    private String farmerName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponse from(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .unit(p.getUnit())
                .stock(p.getStock())
                .imageUrl(p.getImageUrl())
                .origin(p.getOrigin())
                .shippingMethod(p.getShippingMethod())
                .groupBuyEnabled(p.getGroupBuyEnabled())
                .status(p.getStatus())
                .categoryId(p.getCategory().getId())
                .categoryName(p.getCategory().getName())
                .farmerId(p.getFarmer().getId())
                .farmerName(p.getFarmer().getName())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
