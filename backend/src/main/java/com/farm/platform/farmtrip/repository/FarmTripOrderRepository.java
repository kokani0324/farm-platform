package com.farm.platform.farmtrip.repository;

import com.farm.platform.account.entity.Farmer;
import com.farm.platform.farmtrip.entity.FarmTripOrder;
import com.farm.platform.farmtrip.entity.FarmTripOrderStatus;
import com.farm.platform.farmtrip.entity.FarmTripSession;
import com.farm.platform.account.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FarmTripOrderRepository extends JpaRepository<FarmTripOrder, Long> {

    @Query("select o from FarmTripOrder o " +
            "join fetch o.session s join fetch s.farmTrip t join fetch t.farmer " +
            "join fetch o.user " +
            "where o.id = :id")
    Optional<FarmTripOrder> findFullById(Long id);

    Page<FarmTripOrder> findByUserOrderByBookedAtDesc(Member user, Pageable pageable);

    Page<FarmTripOrder> findBySession_FarmTrip_FarmerOrderByBookedAtDesc(Farmer farmer, Pageable pageable);

    Page<FarmTripOrder> findBySession_FarmTrip_FarmerAndStatusOrderByBookedAtDesc(Farmer farmer, FarmTripOrderStatus status, Pageable pageable);

    boolean existsByBookingNo(String bookingNo);

    List<FarmTripOrder> findBySessionAndStatus(FarmTripSession session, FarmTripOrderStatus status);

    boolean existsBySession_FarmTripAndUserAndStatus(
            com.farm.platform.farmtrip.entity.FarmTrip trip,
            Member user,
            FarmTripOrderStatus status);
}
