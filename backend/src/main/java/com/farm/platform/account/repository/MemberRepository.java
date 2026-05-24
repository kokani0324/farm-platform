package com.farm.platform.account.repository;

import com.farm.platform.account.entity.AccountStatus;
import com.farm.platform.account.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);

    long countByStatus(AccountStatus status);

    @Query("select m from Member m where (:keyword is null or :keyword = '' " +
            "or lower(m.email) like lower(concat('%', :keyword, '%')) " +
            "or lower(m.name) like lower(concat('%', :keyword, '%'))) " +
            "order by m.createdAt desc")
    Page<Member> searchAdminList(@Param("keyword") String keyword, Pageable pageable);
}
