package com.farm.platform.entity;

/** 帳號權限狀態(對應 spec USER.user_status / FARMER.farmer_status / ADMIN.admin_status) */
public enum AccountStatus {
    NORMAL,
    WARNED,
    SUSPENDED,
    DELETED
}
