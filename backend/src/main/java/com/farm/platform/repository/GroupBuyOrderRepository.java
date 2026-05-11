package com.farm.platform.repository;

import com.farm.platform.entity.GroupBuy;
import com.farm.platform.entity.GroupBuyOrder;
import com.farm.platform.entity.GroupBuyOrderStatus;
import com.farm.platform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupBuyOrderRepository extends JpaRepository<GroupBuyOrder, Long> {

    boolean existsByOrderNo(String orderNo);

    @EntityGraph(attributePaths = {"groupBuy", "groupBuy.product", "host", "farmer", "participations", "participations.user"})
    Optional<GroupBuyOrder> findByGroupBuy(GroupBuy groupBuy);

    @EntityGraph(attributePaths = {"groupBuy", "groupBuy.product", "host", "farmer"})
    Page<GroupBuyOrder> findByHostOrderByCreatedAtDesc(User host, Pageable pageable);

    @EntityGraph(attributePaths = {"groupBuy", "groupBuy.product", "host", "farmer"})
    Page<GroupBuyOrder> findByFarmerOrderByCreatedAtDesc(User farmer, Pageable pageable);

    long countByStatus(GroupBuyOrderStatus status);
}
