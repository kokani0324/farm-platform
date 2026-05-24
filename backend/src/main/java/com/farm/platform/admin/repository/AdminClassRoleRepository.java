package com.farm.platform.admin.repository;

import com.farm.platform.admin.entity.AdminClassRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminClassRoleRepository extends JpaRepository<AdminClassRole, AdminClassRole.Key> {
    List<AdminClassRole> findByClassId(Long classId);
}
