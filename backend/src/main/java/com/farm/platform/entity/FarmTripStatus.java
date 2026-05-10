package com.farm.platform.entity;

public enum FarmTripStatus {
    ACTIVE,     // 招募中
    FULL,       // 已額滿(到達 capacity)
    CLOSED,     // 報名截止
    CANCELLED,  // 小農取消
    COMPLETED   // 活動已舉辦完畢
}
