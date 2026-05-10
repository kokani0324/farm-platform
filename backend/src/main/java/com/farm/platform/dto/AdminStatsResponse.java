package com.farm.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long totalConsumers;
    private long totalFarmers;
    private long totalAdmins;
    private long disabledUsers;
    private long totalProducts;
    private long activeProducts;
    private long totalOrders;
    private long totalGroupBuys;
    private long openGroupBuys;
    private long totalFarmTrips;
    private long totalBlogs;
    private long pendingBlogReports;
}
