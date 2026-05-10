package com.farm.platform.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateFarmTripBookingRequest {

    @NotNull
    @Min(value = 1, message = "至少 1 人")
    @Max(value = 99, message = "單次預約最多 99 人")
    private Integer numPeople;

    @NotBlank
    @Size(max = 50)
    private String contactName;

    @NotBlank
    @Size(max = 20)
    private String contactPhone;

    @Size(max = 500)
    private String note;
}
