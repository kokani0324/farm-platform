package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "blog_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BlogType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(length = 10)
    private String icon;

    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    /** 是否僅限小農可發表（例如「產地日記」） */
    @Column(name = "farmer_only", nullable = false, columnDefinition = "BIT(1) DEFAULT 0")
    @Builder.Default
    private Boolean farmerOnly = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
