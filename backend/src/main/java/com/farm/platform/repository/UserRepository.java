package com.farm.platform.repository;

import com.farm.platform.entity.Role;
import com.farm.platform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRoleOrderByCreatedAtDesc(Role role, Pageable pageable);

    @Query("select u from User u where (:keyword is null or :keyword = '' " +
            "or lower(u.email) like lower(concat('%', :keyword, '%')) " +
            "or lower(u.name) like lower(concat('%', :keyword, '%'))) " +
            "order by u.createdAt desc")
    Page<User> searchAdminList(String keyword, Pageable pageable);

    long countByRole(Role role);

    long countByEnabledFalse();
}
