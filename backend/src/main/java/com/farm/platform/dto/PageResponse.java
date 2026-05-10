package com.farm.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * 分頁回應的薄包裝，避免 Spring 預設的 PageImpl JSON 結構不穩定警告
 */
@Data
@Builder
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page;          // 0-based
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public static <S, T> PageResponse<T> of(Page<S> page, Function<S, T> mapper) {
        return PageResponse.<T>builder()
                .content(page.getContent().stream().map(mapper).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
