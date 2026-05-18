package com.farm.platform.dto;

import com.farm.platform.entity.Product;
import com.farm.platform.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/** 文章中介紹的商品卡資料 (極簡，足夠 link / 加購) */
@Data
@Builder
@AllArgsConstructor
public class BlogProductMini {
    private Long id;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private String unit;
    private Boolean available;

    public static BlogProductMini from(Product p) {
        return BlogProductMini.builder()
                .id(p.getId())
                .name(p.getName())
                .imageUrl(p.getImageUrl())
                .price(p.getPrice())
                .unit(p.getUnit())
                .available(p.getStatus() == ProductStatus.ACTIVE && p.getStock() > 0)
                .build();
    }
}
