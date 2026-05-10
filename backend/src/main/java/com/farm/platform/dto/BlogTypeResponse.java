package com.farm.platform.dto;

import com.farm.platform.entity.BlogType;
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

    public static BlogTypeResponse from(BlogType t) {
        return BlogTypeResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .description(t.getDescription())
                .icon(t.getIcon())
                .sortOrder(t.getSortOrder())
                .build();
    }
}
