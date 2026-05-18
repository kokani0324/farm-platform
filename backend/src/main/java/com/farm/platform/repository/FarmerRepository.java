package com.farm.platform.repository;

import com.farm.platform.entity.AccountStatus;
import com.farm.platform.entity.Farmer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmerRepository extends JpaRepository<Farmer, Long> {
    Optional<Farmer> findByEmail(String email);
    boolean existsByEmail(String email);

    long countByStatus(AccountStatus status);
    long countByCertPassed(boolean certPassed);

    @Query("select f from Farmer f where (:keyword is null or :keyword = '' " +
            "or lower(f.email) like lower(concat('%', :keyword, '%')) " +
            "or lower(f.farmName) like lower(concat('%', :keyword, '%'))) " +
            "order by f.createdAt desc")
    Page<Farmer> searchAdminList(@Param("keyword") String keyword, Pageable pageable);

    List<Farmer> findByCertPassedFalseOrderByCreatedAtDesc();
}
