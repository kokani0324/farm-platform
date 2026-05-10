package com.farm.platform.repository;

import com.farm.platform.entity.FarmTrip;
import com.farm.platform.entity.FarmTripBooking;
import com.farm.platform.entity.FarmTripBookingStatus;
import com.farm.platform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FarmTripBookingRepository extends JpaRepository<FarmTripBooking, Long> {

    @Query("select b from FarmTripBooking b join fetch b.farmTrip t join fetch t.farmer join fetch t.category where b.id = :id")
    Optional<FarmTripBooking> findFullById(Long id);

    Page<FarmTripBooking> findByUserOrderByBookedAtDesc(User user, Pageable pageable);

    Page<FarmTripBooking> findByFarmTrip_FarmerOrderByBookedAtDesc(User farmer, Pageable pageable);

    Page<FarmTripBooking> findByFarmTrip_FarmerAndStatusOrderByBookedAtDesc(User farmer, FarmTripBookingStatus status, Pageable pageable);

    boolean existsByBookingNo(String bookingNo);

    List<FarmTripBooking> findByFarmTripAndStatus(FarmTrip farmTrip, FarmTripBookingStatus status);
}
