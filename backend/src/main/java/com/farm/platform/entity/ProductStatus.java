package com.farm.platform.entity;

/**
 * 商品狀態
 * ACTIVE  上架中
 * INACTIVE 下架（小農自行下架）
 * SOLD_OUT 售完（庫存=0 自動）
 */
public enum ProductStatus {
    ACTIVE,
    INACTIVE,
    SOLD_OUT
}
