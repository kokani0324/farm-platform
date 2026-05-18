package com.farm.platform.dto;

import com.farm.platform.entity.FarmTripAuditStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FarmTripAuditRequest {

    /** 只允許 APPROVED 或 REJECTED */
    @NotNull
    private FarmTripAuditStatus decision;

    @Size(max = 200)
    private String reason;
}
