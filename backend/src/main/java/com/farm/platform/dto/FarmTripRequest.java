package com.farm.platform.dto;

import com.farm.platform.entity.TripType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FarmTripRequest {

    @NotNull
    private Long categoryId;

    @NotNull
    private TripType tripType;

    @NotBlank
    @Size(max = 30)
    private String title;

    @Size(max = 500)
    private String intro;

    @Size(max = 500)
    private String imageUrl;

    @Size(max = 100)
    private String location;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal price;

    @NotNull
    @Min(value = 1)
    private Integer capacity;

    @NotNull
    private LocalDateTime tripStart;

    @NotNull
    private LocalDateTime tripEnd;

    @NotNull
    private LocalDateTime bookStart;

    @NotNull
    private LocalDateTime bookEnd;
}
