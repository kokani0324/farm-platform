package com.farm.platform.repository;

import com.farm.platform.entity.BlogType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlogTypeRepository extends JpaRepository<BlogType, Long> {
    List<BlogType> findAllByOrderBySortOrderAsc();
    Optional<BlogType> findByName(String name);
    boolean existsByName(String name);
}
