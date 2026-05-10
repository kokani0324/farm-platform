package com.farm.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogReportRequest {
    @NotBlank
    @Size(max = 100)
    private String reason;
}
