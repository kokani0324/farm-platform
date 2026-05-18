package com.farm.platform.repository;

import com.farm.platform.entity.FarmTrip;
import com.farm.platform.entity.FarmTripComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmTripCommentRepository extends JpaRepository<FarmTripComment, Long> {

    Page<FarmTripComment> findByFarmTripOrderByCreatedAtDesc(FarmTrip farmTrip, Pageable pageable);

    boolean existsByFarmTripAndUser(FarmTrip farmTrip, com.farm.platform.entity.Member user);
}
