package com.farm.platform.repository;

import com.farm.platform.entity.AdminClassRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminClassRoleRepository extends JpaRepository<AdminClassRole, AdminClassRole.Key> {
    List<AdminClassRole> findByClassId(Long classId);
}
