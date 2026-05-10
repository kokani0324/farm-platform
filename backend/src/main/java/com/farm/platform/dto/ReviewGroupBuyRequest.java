package com.farm.platform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewGroupBuyRequest {
    @NotNull
    private Boolean approved;

    @Size(max = 500)
    private String rejectReason;
}
