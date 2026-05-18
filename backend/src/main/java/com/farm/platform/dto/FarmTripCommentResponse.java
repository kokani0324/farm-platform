package com.farm.platform.dto;

import com.farm.platform.entity.FarmTripComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class FarmTripCommentResponse {
    private Long id;
    private Long farmTripId;
    private Long userId;
    private String userName;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;

    public static FarmTripCommentResponse from(FarmTripComment c) {
        return FarmTripCommentResponse.builder()
                .id(c.getId())
                .farmTripId(c.getFarmTrip().getId())
                .userId(c.getUser().getId())
                .userName(c.getUser().getName())
                .rating(c.getRating())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
