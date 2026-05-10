package com.farm.platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = "code")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 分類代碼，如 "vegetable" */
    @Column(nullable = false, length = 30)
    private String code;

    /** 顯示名稱，如「葉菜類」 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 圖示 emoji 或 icon 名稱 */
    @Column(length = 30)
    private String icon;

    /** 排序，數字越小越前面 */
    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
