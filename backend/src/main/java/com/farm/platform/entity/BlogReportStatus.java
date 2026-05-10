package com.farm.platform.entity;

/** 對應 spec BLOG_REPORT.report_status: 0 未處理 / 1 已處理顯示 / 2 已處理不顯示 */
public enum BlogReportStatus {
    PENDING,        // 0 未處理
    KEEP,           // 1 已處理 顯示
    HIDDEN          // 2 已處理 不顯示
}
