package com.farm.platform.entity;

public enum GroupBuyOrderStatus {
    /** 成團後尚未付款 */
    PENDING_PAYMENT,
    /** 團主已付款 */
    PAID,
    /** 已全部出貨（小農側） */
    SHIPPING,
    /** 全部團員已收貨 */
    COMPLETED,
    /** 取消（保留：糾紛、退款後置） */
    CANCELLED
}
