package com.farm.platform.repository;

import com.farm.platform.entity.News;
import com.farm.platform.entity.NewsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long> {

    Page<News> findByStatusOrderByPublishedAtDesc(NewsStatus status, Pageable pageable);

    Page<News> findAllByOrderByPublishedAtDesc(Pageable pageable);

    @Query("select n from News n left join fetch n.admin where n.id = :id")
    Optional<News> findFullById(Long id);
}
