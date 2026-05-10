package com.farm.platform.repository;

import com.farm.platform.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderBySortOrderAsc();

    Optional<Category> findByCode(String code);

    boolean existsByCode(String code);
}
