package com.farm.platform.dto;

import com.farm.platform.entity.FarmTrip;
import com.farm.platform.entity.FarmTripStatus;
import com.farm.platform.entity.TripType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class FarmTripResponse {
    private Long id;
    private String title;
    private String intro;
    private String imageUrl;
    private TripType tripType;
    private String location;
    private BigDecimal price;
    private Integer capacity;
    private Integer currentBookings;
    private Integer remaining;

    private LocalDateTime tripStart;
    private LocalDateTime tripEnd;
    private LocalDateTime bookStart;
    private LocalDateTime bookEnd;

    private FarmTripStatus status;

    private Long categoryId;
    private String categoryName;

    private Long farmerId;
    private String farmerName;

    private LocalDateTime createdAt;

    public static FarmTripResponse from(FarmTrip t) {
        return FarmTripResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .intro(t.getIntro())
                .imageUrl(t.getImageUrl())
                .tripType(t.getTripType())
                .location(t.getLocation())
                .price(t.getPrice())
                .capacity(t.getCapacity())
                .currentBookings(t.getCurrentBookings())
                .remaining(t.remainingCapacity())
                .tripStart(t.getTripStart())
                .tripEnd(t.getTripEnd())
                .bookStart(t.getBookStart())
                .bookEnd(t.getBookEnd())
                .status(t.getStatus())
                .categoryId(t.getCategory().getId())
                .categoryName(t.getCategory().getName())
                .farmerId(t.getFarmer().getId())
                .farmerName(t.getFarmer().getName())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
