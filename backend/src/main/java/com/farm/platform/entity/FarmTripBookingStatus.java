package com.farm.platform.entity;

public enum FarmTripBookingStatus {
    PENDING_PAYMENT,  // 待付款(預設;貨到付款場景則同步建立 CONFIRMED)
    CONFIRMED,        // 已確認
    CANCELLED,        // 已取消
    COMPLETED         // 已完成(活動結束後)
}
