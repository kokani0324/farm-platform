package com.farm.platform.entity;

public enum PaymentMethod {
    /** 模擬信用卡（demo 用，按確認即視為已付款） */
    CREDIT_CARD_SIM,
    /** 模擬 ATM 轉帳 */
    BANK_TRANSFER_SIM,
    /** 貨到付款 */
    CASH_ON_DELIVERY
}
