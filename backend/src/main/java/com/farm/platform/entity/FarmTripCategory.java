package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** 體驗活動類別(對應 spec ER 圖「體驗活動類別」) */
@Entity
@Table(name = "farm_trip_categories", indexes = {
        @Index(name = "uk_ftc_code", columnList = "code", unique = true),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FarmTripCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, unique = true)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 10)
    private String icon;

    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
