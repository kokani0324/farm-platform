package com.farm.platform.repository;

import com.farm.platform.entity.FarmTrip;
import com.farm.platform.entity.FarmTripAudit;
import com.farm.platform.entity.FarmTripAuditStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FarmTripAuditRepository extends JpaRepository<FarmTripAudit, Long> {

    List<FarmTripAudit> findByFarmTripOrderByCreatedAtDesc(FarmTrip farmTrip);

    Optional<FarmTripAudit> findFirstByFarmTripAndStatusOrderByCreatedAtDesc(FarmTrip farmTrip, FarmTripAuditStatus status);

    Page<FarmTripAudit> findByStatusOrderByCreatedAtAsc(FarmTripAuditStatus status, Pageable pageable);
}
