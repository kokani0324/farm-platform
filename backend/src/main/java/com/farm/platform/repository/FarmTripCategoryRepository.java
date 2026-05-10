package com.farm.platform.repository;

import com.farm.platform.entity.FarmTripCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FarmTripCategoryRepository extends JpaRepository<FarmTripCategory, Long> {
    List<FarmTripCategory> findAllByOrderBySortOrderAsc();
    Optional<FarmTripCategory> findByCode(String code);
    boolean existsByCode(String code);
}
