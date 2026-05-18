package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 小農會員,對應 spec FARMER 表;送審制(cert_passed=false 預設,管理員審核後才啟用) */
@Entity
@Table(name = "farmers", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Farmer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "farmer_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    /** Phase A 暫拿掉 CITY_DISTRICT FK,改 free-text */
    @Column(name = "farm_address", nullable = false, length = 100)
    private String farmAddress;

    @Column(name = "farm_name", length = 50)
    private String farmName;

    @Column(name = "loc_lat", precision = 10, scale = 7)
    private BigDecimal locLat;

    @Column(name = "loc_long", precision = 10, scale = 7)
    private BigDecimal locLong;

    @Column(name = "farm_desc", columnDefinition = "TEXT")
    private String farmDesc;

    @Column(name = "farmer_phone_num", length = 20)
    private String phone;

    /** 文件類型(身分 / 土地 / 農產品) */
    @Enumerated(EnumType.STRING)
    @Column(name = "cert_type", length = 20)
    private FarmerCertType certType;

    /** 送審文件本體(LONGBLOB);demo 階段可暫存檔名/Base64 */
    @Lob
    @Column(name = "cert_file", columnDefinition = "LONGBLOB")
    private byte[] certFile;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    /** 是否已通過審核(預設 false,管理員審核通過後改 true 才能登入) */
    @Column(name = "cert_passed", nullable = false)
    @Builder.Default
    private Boolean certPassed = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "farmer_status", nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.NORMAL;

    @CreationTimestamp
    @Column(name = "farmer_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
