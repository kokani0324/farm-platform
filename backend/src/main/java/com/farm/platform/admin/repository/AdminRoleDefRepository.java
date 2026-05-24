package com.farm.platform.admin.repository;

import com.farm.platform.admin.entity.AdminRoleDef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRoleDefRepository extends JpaRepository<AdminRoleDef, Long> {
    Optional<AdminRoleDef> findByName(String name);
}
