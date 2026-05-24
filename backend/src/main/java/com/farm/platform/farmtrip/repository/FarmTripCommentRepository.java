package com.farm.platform.farmtrip.repository;

import com.farm.platform.farmtrip.entity.FarmTrip;
import com.farm.platform.farmtrip.entity.FarmTripComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmTripCommentRepository extends JpaRepository<FarmTripComment, Long> {

    Page<FarmTripComment> findByFarmTripOrderByCreatedAtDesc(FarmTrip farmTrip, Pageable pageable);

    boolean existsByFarmTripAndUser(FarmTrip farmTrip, com.farm.platform.account.entity.Member user);
}
