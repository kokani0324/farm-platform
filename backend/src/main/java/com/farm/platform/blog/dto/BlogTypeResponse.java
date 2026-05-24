package com.farm.platform.blog.dto;

import com.farm.platform.blog.entity.BlogType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BlogTypeResponse {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private Integer sortOrder;
    /** 是否僅限小農可發表（前端按 type 篩 dropdown） */
    private Boolean farmerOnly;

    public static BlogTypeResponse from(BlogType t) {
        return BlogTypeResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .description(t.getDescription())
                .icon(t.getIcon())
                .sortOrder(t.getSortOrder())
                .farmerOnly(Boolean.TRUE.equals(t.getFarmerOnly()))
                .build();
    }
}
