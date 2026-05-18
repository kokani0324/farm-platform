package com.farm.platform.repository;

import com.farm.platform.entity.Farmer;
import com.farm.platform.entity.FarmTrip;
import com.farm.platform.entity.FarmTripStatus;
import com.farm.platform.entity.TripType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FarmTripRepository extends JpaRepository<FarmTrip, Long> {

    @Query("select t from FarmTrip t join fetch t.farmer where t.id = :id")
    Optional<FarmTrip> findFullById(Long id);

    Page<FarmTrip> findByStatusInOrderByCreatedAtDesc(List<FarmTripStatus> statuses, Pageable pageable);

    Page<FarmTrip> findByTripTypeAndStatusInOrderByCreatedAtDesc(TripType tripType, List<FarmTripStatus> statuses, Pageable pageable);

    Page<FarmTrip> findByFarmerOrderByCreatedAtDesc(Farmer farmer, Pageable pageable);

    Page<FarmTrip> findByStatusOrderByCreatedAtDesc(FarmTripStatus status, Pageable pageable);

    boolean existsByTitleAndFarmer(String title, Farmer farmer);
}
