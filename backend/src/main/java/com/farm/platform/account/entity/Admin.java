package com.farm.platform.account.entity;

import com.farm.platform.admin.entity.AdminClass;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/** 管理員(對應 spec ADMIN);隸屬一個 AdminClass(權限類別) */
@Entity
@Table(name = "admins", uniqueConstraints = {
        @UniqueConstraint(columnNames = "admin_email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long id;

    @Column(name = "class_id")
    private Long classId;

    /** 建立者(自我參考):由哪位管理員建立 */
    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "admin_email", nullable = false, length = 50)
    private String email;

    @Column(name = "admin_password", nullable = false, length = 255)
    private String password;

    @Column(name = "admin_name", nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_status", nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.NORMAL;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
