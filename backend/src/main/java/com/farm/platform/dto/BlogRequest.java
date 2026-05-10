package com.farm.platform.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BlogRequest {

    @NotNull
    private Long blogTypeId;

    @NotBlank
    @Size(max = 60, message = "標題不可超過 60 字")
    private String title;

    @NotBlank
    @Size(max = 50000, message = "內容過長")
    private String content;

    @Size(max = 500)
    private String coverImageUrl;
}
