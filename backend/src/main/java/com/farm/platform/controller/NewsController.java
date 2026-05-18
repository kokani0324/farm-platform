package com.farm.platform.controller;

import com.farm.platform.dto.NewsResponse;
import com.farm.platform.dto.PageResponse;
import com.farm.platform.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService service;

    @GetMapping
    public PageResponse<NewsResponse> list(@PageableDefault(size = 12) Pageable pageable) {
        return service.listPublic(pageable);
    }

    @GetMapping("/{id}")
    public NewsResponse detail(@PathVariable Long id) {
        return service.getDetail(id);
    }
}
