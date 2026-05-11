package com.farm.platform.entity;

public enum PaymentStatus {
    /** 尚未付款（保留：未來若改用第三方金流帶 callback 時用） */
    PENDING,
    /** 已付款（mock） */
    PAID,
    /** 已退款（退出 / 團購失敗） */
    REFUNDED
}
