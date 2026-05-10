package com.farm.platform.dto;

import com.farm.platform.entity.BlogReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HandleReportRequest {
    /** KEEP(顯示) / HIDDEN(隱藏目標) */
    @NotNull
    private BlogReportStatus action;
}
