package com.farm.platform.repository;

import com.farm.platform.entity.Farmer;
import com.farm.platform.entity.Member;
import com.farm.platform.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "items.product", "farmer"})
    Page<Order> findByConsumerOrderByCreatedAtDesc(Member consumer, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product", "consumer"})
    Page<Order> findByFarmerOrderByCreatedAtDesc(Farmer farmer, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product", "consumer", "farmer"})
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findFullById(@Param("id") Long id);

    boolean existsByOrderNo(String orderNo);
}
