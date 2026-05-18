package com.farm.platform.service;

import com.farm.platform.dto.NewsRequest;
import com.farm.platform.dto.NewsResponse;
import com.farm.platform.dto.PageResponse;
import com.farm.platform.entity.Admin;
import com.farm.platform.entity.News;
import com.farm.platform.entity.NewsStatus;
import com.farm.platform.repository.AdminRepository;
import com.farm.platform.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private final NewsRepository newsRepo;
    private final AdminRepository adminRepo;

    public PageResponse<NewsResponse> listPublic(Pageable pageable) {
        Page<News> page = newsRepo.findByStatusOrderByPublishedAtDesc(NewsStatus.PUBLISHED, pageable);
        return PageResponse.of(page, NewsResponse::summary);
    }

    public NewsResponse getDetail(Long id) {
        News n = newsRepo.findFullById(id)
                .orElseThrow(() -> new IllegalArgumentException("最新消息不存在"));
        if (n.getStatus() != NewsStatus.PUBLISHED) {
            throw new IllegalStateException("此則消息未公開");
        }
        return NewsResponse.from(n);
    }

    /* ===== admin ===== */

    public PageResponse<NewsResponse> adminList(Pageable pageable) {
        return PageResponse.of(newsRepo.findAllByOrderByPublishedAtDesc(pageable), NewsResponse::summary);
    }

    public NewsResponse adminGet(Long id) {
        News n = newsRepo.findFullById(id)
                .orElseThrow(() -> new IllegalArgumentException("最新消息不存在"));
        return NewsResponse.from(n);
    }

    @Transactional
    public NewsResponse create(String adminEmail, NewsRequest req) {
        Admin admin = getAdmin(adminEmail);
        News n = News.builder()
                .title(req.getTitle())
                .summary(req.getSummary())
                .content(req.getContent())
                .coverImageUrl(req.getCoverImageUrl())
                .status(req.getStatus() != null ? req.getStatus() : NewsStatus.PUBLISHED)
                .admin(admin)
                .publishedAt(req.getPublishedAt() != null ? req.getPublishedAt() : LocalDateTime.now())
                .build();
        return NewsResponse.from(newsRepo.save(n));
    }

    @Transactional
    public NewsResponse update(String adminEmail, Long id, NewsRequest req) {
        Admin admin = getAdmin(adminEmail);
        News n = newsRepo.findFullById(id)
                .orElseThrow(() -> new IllegalArgumentException("最新消息不存在"));
        n.setTitle(req.getTitle());
        n.setSummary(req.getSummary());
        n.setContent(req.getContent());
        n.setCoverImageUrl(req.getCoverImageUrl());
        if (req.getStatus() != null) n.setStatus(req.getStatus());
        if (req.getPublishedAt() != null) n.setPublishedAt(req.getPublishedAt());
        n.setAdmin(admin);
        return NewsResponse.from(n);
    }

    @Transactional
    public void delete(Long id) {
        News n = newsRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("最新消息不存在"));
        newsRepo.delete(n);
    }

    private Admin getAdmin(String email) {
        return adminRepo.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("管理員不存在"));
    }
}
