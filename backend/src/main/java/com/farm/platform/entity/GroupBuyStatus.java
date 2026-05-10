package com.farm.platform.entity;

/** 團購活動狀態 */
public enum GroupBuyStatus {
    /** 招募中 */
    OPEN,
    /** 已成團(達到目標數量,進入出貨流程) */
    SUCCESS,
    /** 未達標,結束時取消 */
    FAILED,
    /** 主動取消(小農或團主) */
    CANCELLED
}
