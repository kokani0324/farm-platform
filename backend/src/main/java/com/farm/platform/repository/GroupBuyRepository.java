package com.farm.platform.repository;

import com.farm.platform.entity.GroupBuy;
import com.farm.platform.entity.GroupBuyStatus;
import com.farm.platform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupBuyRepository extends JpaRepository<GroupBuy, Long> {

    @EntityGraph(attributePaths = {"product", "farmer", "host"})
    Page<GroupBuy> findByStatusOrderByDeadlineDateAsc(GroupBuyStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "host", "participations"})
    Page<GroupBuy> findByFarmerOrderByCreatedAtDesc(User farmer, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "farmer", "host", "participations"})
    @Query("select gb from GroupBuy gb where gb.id = :id")
    Optional<GroupBuy> findFullById(@Param("id") Long id);

    /** 排程用:抓出已過期但還停在 OPEN 的團 */
    List<GroupBuy> findByStatusAndDeadlineDateBefore(GroupBuyStatus status, LocalDateTime now);

    long countByStatus(GroupBuyStatus status);
}
