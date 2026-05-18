package com.farm.platform.repository;

import com.farm.platform.entity.AdminClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminClassRepository extends JpaRepository<AdminClass, Long> {
    Optional<AdminClass> findByName(String name);
}
