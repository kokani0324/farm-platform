package com.farm.platform.farmtrip.repository;

import com.farm.platform.farmtrip.entity.FarmTrip;
import com.farm.platform.farmtrip.entity.FarmTripSession;
import com.farm.platform.farmtrip.entity.FarmTripSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FarmTripSessionRepository extends JpaRepository<FarmTripSession, Long> {

    @Query("select s from FarmTripSession s join fetch s.farmTrip t join fetch t.farmer where s.id = :id")
    Optional<FarmTripSession> findFullById(Long id);

    List<FarmTripSession> findByFarmTripOrderByTripStartAsc(FarmTrip farmTrip);

    List<FarmTripSession> findByFarmTripAndStatusOrderByTripStartAsc(FarmTrip farmTrip, FarmTripSessionStatus status);

    List<FarmTripSession> findByStatusAndTripEndBefore(FarmTripSessionStatus status, LocalDateTime time);
}
