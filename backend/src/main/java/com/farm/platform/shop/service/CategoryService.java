package com.farm.platform.shop.service;

import com.farm.platform.shop.dto.CategoryResponse;
import com.farm.platform.shop.entity.Category;
import com.farm.platform.shop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> listAll() {
        return categoryRepository.findAllByOrderBySortOrderAsc()
                .stream().map(CategoryResponse::from).toList();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分類不存在 id=" + id));
    }
}
