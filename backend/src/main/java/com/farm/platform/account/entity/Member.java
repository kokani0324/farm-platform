package com.farm.platform.account.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/** 一般會員(消費者),對應 spec USER 表;與 FARMER 完全獨立,各自 PK 與密碼。 */
@Entity
@Table(name = "members", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false, length = 255) // BCrypt 後 60 char,留寬給未來
    private String password;

    @Column(name = "user_name", nullable = false, length = 50)
    private String name;

    @Column(name = "user_nickname", length = 50)
    private String nickname;

    @Column(name = "user_phone_num", length = 20)
    private String phone;

    /** Phase A 暫拿掉 CITY_DISTRICT FK,改 free-text 地址 */
    @Column(name = "user_address", length = 100)
    private String address;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "memb_level", nullable = false, length = 20)
    @Builder.Default
    private MembershipLevel level = MembershipLevel.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.NORMAL;

    /** spec 同名欄位:標記此會員是否同時也是小農(實際 FARMER 紀錄獨立存在 farmers 表,以 email 識別) */
    @Column(name = "is_farmer", nullable = false)
    @Builder.Default
    private Boolean isFarmer = false;

    @CreationTimestamp
    @Column(name = "user_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
