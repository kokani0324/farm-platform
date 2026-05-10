package com.farm.platform.repository;

import com.farm.platform.entity.GroupBuyRequest;
import com.farm.platform.entity.GroupBuyRequestStatus;
import com.farm.platform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupBuyRequestRepository extends JpaRepository<GroupBuyRequest, Long> {

    @EntityGraph(attributePaths = {"product", "farmer"})
    Page<GroupBuyRequest> findByInitiatorOrderByRequestedAtDesc(User initiator, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "initiator"})
    Page<GroupBuyRequest> findByFarmerAndStatusOrderByRequestedAtDesc(User farmer, GroupBuyRequestStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "initiator"})
    Page<GroupBuyRequest> findByFarmerOrderByRequestedAtDesc(User farmer, Pageable pageable);

    List<GroupBuyRequest> findByInitiatorAndStatus(User initiator, GroupBuyRequestStatus status);
}
