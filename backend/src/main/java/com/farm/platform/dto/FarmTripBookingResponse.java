package com.farm.platform.dto;

import com.farm.platform.entity.FarmTripBooking;
import com.farm.platform.entity.FarmTripBookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class FarmTripBookingResponse {
    private Long id;
    private String bookingNo;

    private Long farmTripId;
    private String farmTripTitle;
    private String farmTripImageUrl;
    private LocalDateTime farmTripStart;
    private LocalDateTime farmTripEnd;
    private String farmTripLocation;

    private Long userId;
    private String userName;

    private Long farmerId;
    private String farmerName;

    private Integer numPeople;
    private BigDecimal totalAmount;
    private String contactName;
    private String contactPhone;
    private String note;

    private FarmTripBookingStatus status;
    private LocalDateTime bookedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;

    public static FarmTripBookingResponse from(FarmTripBooking b) {
        var t = b.getFarmTrip();
        return FarmTripBookingResponse.builder()
                .id(b.getId())
                .bookingNo(b.getBookingNo())
                .farmTripId(t.getId())
                .farmTripTitle(t.getTitle())
                .farmTripImageUrl(t.getImageUrl())
                .farmTripStart(t.getTripStart())
                .farmTripEnd(t.getTripEnd())
                .farmTripLocation(t.getLocation())
                .userId(b.getUser().getId())
                .userName(b.getUser().getName())
                .farmerId(t.getFarmer().getId())
                .farmerName(t.getFarmer().getName())
                .numPeople(b.getNumPeople())
                .totalAmount(b.getTotalAmount())
                .contactName(b.getContactName())
                .contactPhone(b.getContactPhone())
                .note(b.getNote())
                .status(b.getStatus())
                .bookedAt(b.getBookedAt())
                .cancelledAt(b.getCancelledAt())
                .completedAt(b.getCompletedAt())
                .build();
    }
}
