package com.farm.platform.admin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 管理類別(對應 spec ADMIN_CLASS):e.g. 超級管理員 / 一般管理員 / 審核員 / 爭議處理員。 */
@Entity
@Table(name = "admin_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_id")
    private Long id;

    @Column(name = "class_name", nullable = false, length = 30)
    private String name;

    @Column(length = 100)
    private String description;
}
