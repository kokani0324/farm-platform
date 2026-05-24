package com.farm.platform.blog.dto;

import com.farm.platform.blog.entity.BlogReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HandleReportRequest {
    /** KEEP(顯示) / HIDDEN(隱藏目標) */
    @NotNull
    private BlogReportStatus action;
}
