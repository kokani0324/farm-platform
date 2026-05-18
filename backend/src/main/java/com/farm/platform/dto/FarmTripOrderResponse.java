package com.farm.platform.dto;

import com.farm.platform.entity.FarmTripOrder;
import com.farm.platform.entity.FarmTripOrderStatus;
import com.farm.platform.entity.FarmTripSession;
import com.farm.platform.entity.PricingMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class FarmTripOrderResponse {
    private Long id;
    private String bookingNo;

    private Long sessionId;
    private LocalDateTime sessionTripStart;
    private LocalDateTime sessionTripEnd;

    private Long farmTripId;
    private String farmTripTitle;
    private String farmTripImageUrl;
    private String farmTripLocation;
    private PricingMode pricingMode;

    private Long userId;
    private String userName;
    private Long farmerId;
    private String farmerName;

    private Integer numPeople;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private BigDecimal actualWeight;

    private String contactName;
    private String contactPhone;
    private String note;

    private FarmTripOrderStatus status;
    private LocalDateTime bookedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;

    public static FarmTripOrderResponse from(FarmTripOrder o) {
        FarmTripSession s = o.getSession();
        var t = s.getFarmTrip();
        return FarmTripOrderResponse.builder()
                .id(o.getId())
                .bookingNo(o.getBookingNo())
                .sessionId(s.getId())
                .sessionTripStart(s.getTripStart())
                .sessionTripEnd(s.getTripEnd())
                .farmTripId(t.getId())
                .farmTripTitle(t.getTitle())
                .farmTripImageUrl(t.getImageUrl())
                .farmTripLocation(t.getLocation())
                .pricingMode(t.getPricingMode())
                .userId(o.getUser().getId())
                .userName(o.getUser().getName())
                .farmerId(t.getFarmer().getId())
                .farmerName(t.getFarmer().getFarmName())
                .numPeople(o.getNumPeople())
                .unitPrice(o.getUnitPrice())
                .totalAmount(o.getTotalAmount())
                .actualWeight(o.getActualWeight())
                .contactName(o.getContactName())
                .contactPhone(o.getContactPhone())
                .note(o.getNote())
                .status(o.getStatus())
                .bookedAt(o.getBookedAt())
                .cancelledAt(o.getCancelledAt())
                .completedAt(o.getCompletedAt())
                .build();
    }
}
