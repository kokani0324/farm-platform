package com.farm.platform.news.dto;

import com.farm.platform.news.entity.NewsStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsRequest {

    @NotBlank
    @Size(max = 80)
    private String title;

    @Size(max = 200)
    private String summary;

    @NotBlank
    @Size(max = 50000)
    private String content;

    @Size(max = 500)
    private String coverImageUrl;

    /** 可選；省略則用現在時間 */
    private LocalDateTime publishedAt;

    /** 可選；省略則為 PUBLISHED */
    private NewsStatus status;
}
