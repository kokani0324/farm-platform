package com.farm.platform.farmtrip.entity;

/** 場次狀態 (spec FARM_TRIP_SESSION.session_status) */
public enum FarmTripSessionStatus {
    ACTIVE,      // 報名中
    CANCELLED,   // 已取消
    COMPLETED    // 已截止 / 已完成
}
