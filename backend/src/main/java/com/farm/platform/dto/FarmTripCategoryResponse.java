package com.farm.platform.dto;

import com.farm.platform.entity.FarmTripCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FarmTripCategoryResponse {
    private Long id;
    private String code;
    private String name;
    private String icon;
    private Integer sortOrder;

    public static FarmTripCategoryResponse from(FarmTripCategory c) {
        return FarmTripCategoryResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .icon(c.getIcon())
                .sortOrder(c.getSortOrder())
                .build();
    }
}
