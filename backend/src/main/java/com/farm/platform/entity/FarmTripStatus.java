package com.farm.platform.entity;

/** 體驗活動本體狀態 (spec FARM_TRIP.status) */
public enum FarmTripStatus {
    PENDING,    // 審核中
    REJECTED,   // 審核未通過
    ACTIVE,     // 上架開放中
    CLOSED      // 已下架 / 關閉
}
