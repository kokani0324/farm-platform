package com.farm.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogCommentRequest {
    @NotBlank
    @Size(max = 200)
    private String content;
}
