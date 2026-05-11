package com.farm.platform.entity;

public enum ReceiptStatus {
    /** 尚未出貨 */
    NOT_SHIPPED,
    /** 小農已出貨，等待團員確認 */
    SHIPPED,
    /** 團員已確認收貨 */
    RECEIVED
}
