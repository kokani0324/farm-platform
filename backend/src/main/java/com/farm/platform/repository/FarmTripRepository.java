package com.farm.platform.repository;

import com.farm.platform.entity.FarmTrip;
import com.farm.platform.entity.FarmTripCategory;
import com.farm.platform.entity.FarmTripStatus;
import com.farm.platform.entity.TripType;
import com.farm.platform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FarmTripRepository extends JpaRepository<FarmTrip, Long> {

    @Query("select t from FarmTrip t join fetch t.farmer join fetch t.category where t.id = :id")
    Optional<FarmTrip> findFullById(Long id);

    Page<FarmTrip> findByStatusInOrderByTripStartAsc(List<FarmTripStatus> statuses, Pageable pageable);

    Page<FarmTrip> findByCategoryAndStatusInOrderByTripStartAsc(FarmTripCategory category, List<FarmTripStatus> statuses, Pageable pageable);

    Page<FarmTrip> findByTripTypeAndStatusInOrderByTripStartAsc(TripType tripType, List<FarmTripStatus> statuses, Pageable pageable);

    Page<FarmTrip> findByCategoryAndTripTypeAndStatusInOrderByTripStartAsc(FarmTripCategory category, TripType tripType, List<FarmTripStatus> statuses, Pageable pageable);

    Page<FarmTrip> findByFarmerOrderByCreatedAtDesc(User farmer, Pageable pageable);

    /** 排程: 已過 bookEnd 但仍 ACTIVE/FULL,改 CLOSED;已過 tripEnd 改 COMPLETED */
    List<FarmTrip> findByStatusInAndBookEndBefore(List<FarmTripStatus> statuses, LocalDateTime time);
    List<FarmTrip> findByStatusInAndTripEndBefore(List<FarmTripStatus> statuses, LocalDateTime time);
}
