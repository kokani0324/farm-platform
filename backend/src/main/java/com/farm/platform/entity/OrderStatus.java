package com.farm.platform.entity;

public enum OrderStatus {
    /** 待付款 */
    PENDING_PAYMENT,
    /** 已付款（等待小農出貨） */
    PAID,
    /** 已出貨 */
    SHIPPED,
    /** 已完成 */
    COMPLETED,
    /** 已取消 */
    CANCELLED
}
