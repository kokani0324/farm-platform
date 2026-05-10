package com.farm.platform.service;

import com.farm.platform.dto.CategoryResponse;
import com.farm.platform.entity.Category;
import com.farm.platform.repository.CategoryRepository;
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
