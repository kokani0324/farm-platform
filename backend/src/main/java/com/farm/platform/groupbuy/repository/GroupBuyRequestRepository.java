package com.farm.platform.groupbuy.repository;

import com.farm.platform.account.entity.Farmer;
import com.farm.platform.groupbuy.entity.GroupBuyRequest;
import com.farm.platform.groupbuy.entity.GroupBuyRequestStatus;
import com.farm.platform.account.entity.Member;
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
