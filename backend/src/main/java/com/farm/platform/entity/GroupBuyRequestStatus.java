package com.farm.platform.entity;

/** 團購請求(由消費者發起,等小農審核)的狀態 */
public enum GroupBuyRequestStatus {
    /** 待小農審核 */
    PENDING,
    /** 已通過(已建立 GroupBuy) */
    APPROVED,
    /** 小農拒絕 */
    REJECTED,
    /** 發起者撤回 */
    WITHDRAWN
}
