package com.farm.platform.shop.entity;

/** 體驗活動計價模式 */
public enum PricingMode {
    PER_PERSON,   // 每人定價：下單 total = num_people × session_price
    PER_WEIGHT    // 按採收重量：下單 total_amount = 0，活動結束由小農補登 actual_weight 後結算
}
