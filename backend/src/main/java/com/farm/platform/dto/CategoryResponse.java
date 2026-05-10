package com.farm.platform.dto;

import com.farm.platform.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String code;
    private String name;
    private String icon;
    private Integer sortOrder;

    public static CategoryResponse from(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .icon(c.getIcon())
                .sortOrder(c.getSortOrder())
                .build();
    }
}
