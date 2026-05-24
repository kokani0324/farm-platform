package com.farm.platform.farmtrip.dto;

import com.farm.platform.farmtrip.entity.FarmTripSession;
import com.farm.platform.farmtrip.entity.FarmTripSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class FarmTripSessionResponse {
    private Long id;
    private Long farmTripId;
    private BigDecimal sessionPrice;
    private LocalDateTime tripStart;
    private LocalDateTime tripEnd;
    private LocalDateTime bookStart;
    private LocalDateTime bookEnd;
    private Integer attendance;
    /** 剩餘容量；trip.capacityPerSession 為 null 時回傳 null 表無限 */
    private Integer remaining;
    private FarmTripSessionStatus status;

    public static FarmTripSessionResponse from(FarmTripSession s) {
        Integer cap = s.getFarmTrip().getCapacityPerSession();
        Integer remaining = (cap == null) ? null : Math.max(0, cap - s.getAttendance());
        return FarmTripSessionResponse.builder()
                .id(s.getId())
                .farmTripId(s.getFarmTrip().getId())
                .sessionPrice(s.getSessionPrice())
                .tripStart(s.getTripStart())
                .tripEnd(s.getTripEnd())
                .bookStart(s.getBookStart())
                .bookEnd(s.getBookEnd())
                .attendance(s.getAttendance())
                .remaining(remaining)
                .status(s.getStatus())
                .build();
    }
}
