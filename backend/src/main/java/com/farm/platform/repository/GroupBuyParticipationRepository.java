package com.farm.platform.repository;

import com.farm.platform.entity.GroupBuy;
import com.farm.platform.entity.GroupBuyParticipation;
import com.farm.platform.entity.ParticipationStatus;
import com.farm.platform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupBuyParticipationRepository extends JpaRepository<GroupBuyParticipation, Long> {

    Optional<GroupBuyParticipation> findByGroupBuyAndUser(GroupBuy groupBuy, User user);

    @EntityGraph(attributePaths = {"groupBuy", "groupBuy.product", "groupBuy.farmer"})
    Page<GroupBuyParticipation> findByUserOrderByJoinedAtDesc(User user, Pageable pageable);

    /** 統計團購已加入的數量(只算 JOINED) */
    @Query("select coalesce(sum(p.quantity), 0) from GroupBuyParticipation p " +
            "where p.groupBuy = :gb and p.status = :status")
    int sumQuantityByGroupBuy(@Param("gb") GroupBuy gb, @Param("status") ParticipationStatus status);

    List<GroupBuyParticipation> findByGroupBuyAndStatus(GroupBuy groupBuy, ParticipationStatus status);
}
