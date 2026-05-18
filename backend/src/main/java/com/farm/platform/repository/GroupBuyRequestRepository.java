package com.farm.platform.repository;

import com.farm.platform.entity.Farmer;
import com.farm.platform.entity.GroupBuyRequest;
import com.farm.platform.entity.GroupBuyRequestStatus;
import com.farm.platform.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupBuyRequestRepository extends JpaRepository<GroupBuyRequest, Long> {

    @EntityGraph(attributePaths = {"product", "farmer"})
    Page<GroupBuyRequest> findByInitiatorOrderByRequestedAtDesc(Member initiator, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "initiator"})
    Page<GroupBuyRequest> findByFarmerAndStatusOrderByRequestedAtDesc(Farmer farmer, GroupBuyRequestStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "initiator"})
    Page<GroupBuyRequest> findByFarmerOrderByRequestedAtDesc(Farmer farmer, Pageable pageable);

    List<GroupBuyRequest> findByInitiatorAndStatus(Member initiator, GroupBuyRequestStatus status);
}
