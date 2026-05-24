package com.farm.platform.farmtrip.dto;

import com.farm.platform.farmtrip.entity.FarmTrip;
import com.farm.platform.farmtrip.entity.FarmTripStatus;
import com.farm.platform.shop.entity.PricingMode;
import com.farm.platform.farmtrip.entity.TripType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class FarmTripResponse {
    private Long id;
    private String title;
    private String intro;
    private String imageUrl;
    private TripType tripType;
    private PricingMode pricingMode;
    private String location;
    private BigDecimal price;
    private Integer capacityPerSession;
    private FarmTripStatus status;

    private Long farmerId;
    private String farmerName;

    private Integer ratingCount;
    private Integer ratingTotalStars;
    private Double averageRating;

    /** 詳情頁帶場次清單 (列表頁可為 null) */
    private List<FarmTripSessionResponse> sessions;

    private LocalDateTime createdAt;

    public static FarmTripResponse from(FarmTrip t) {
        return from(t, null);
    }

    public static FarmTripResponse from(FarmTrip t, List<FarmTripSessionResponse> sessions) {
        double avg = t.getRatingCount() > 0
                ? Math.round((double) t.getRatingTotalStars() / t.getRatingCount() * 10.0) / 10.0
                : 0.0;
        return FarmTripResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .intro(t.getIntro())
                .imageUrl(t.getImageUrl())
                .tripType(t.getTripType())
                .pricingMode(t.getPricingMode())
                .location(t.getLocation())
                .price(t.getPrice())
                .capacityPerSession(t.getCapacityPerSession())
                .status(t.getStatus())
                .farmerId(t.getFarmer().getId())
                .farmerName(t.getFarmer().getFarmName())
                .ratingCount(t.getRatingCount())
                .ratingTotalStars(t.getRatingTotalStars())
                .averageRating(avg)
                .sessions(sessions)
                .createdAt(t.getCreatedAt())
                .build();
    }
}
