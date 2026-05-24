package com.farm.platform.news.controller;

import com.farm.platform.news.dto.NewsRequest;
import com.farm.platform.news.dto.NewsResponse;
import com.farm.platform.common.dto.PageResponse;
import com.farm.platform.news.service.NewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/news")
@RequiredArgsConstructor
public class AdminNewsController {

    private final NewsService service;

    @GetMapping
    public PageResponse<NewsResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        return service.adminList(pageable);
    }

    @GetMapping("/{id}")
    public NewsResponse detail(@PathVariable Long id) { return service.adminGet(id); }

    @PostMapping
    public NewsResponse create(@AuthenticationPrincipal UserDetails me, @Valid @RequestBody NewsRequest req) {
        return service.create(me.getUsername(), req);
    }

    @PutMapping("/{id}")
    public NewsResponse update(@AuthenticationPrincipal UserDetails me, @PathVariable Long id, @Valid @RequestBody NewsRequest req) {
        return service.update(me.getUsername(), id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
