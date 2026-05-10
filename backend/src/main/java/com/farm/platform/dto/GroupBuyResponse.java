package com.farm.platform.dto;

import com.farm.platform.entity.GroupBuy;
import com.farm.platform.entity.GroupBuyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class GroupBuyResponse {
    private Long id;

    private Long productId;
    private String productName;
    private String productImageUrl;
    private String productUnit;
    private BigDecimal productPrice;     // 原價(對比團購價)

    private Long farmerId;
    private String farmerName;

    private Long hostId;
    private String hostName;

    private Integer targetQuantity;
    private Integer currentQuantity;
    private Integer remainingQuantity;
    private Integer percent;             // 0-100

    private BigDecimal groupPrice;
    private BigDecimal saving;           // 原價-團購價(每單位)

    private LocalDateTime openDate;
    private LocalDateTime deadlineDate;

    private GroupBuyStatus status;

    private LocalDateTime createdAt;

    /** 當前查詢者是否已加入(JOINED) */
    private Boolean joined;
    private Integer myQuantity;

    public static GroupBuyResponse from(GroupBuy gb, int currentQty, Boolean joined, Integer myQty) {
        int target = gb.getTargetQuantity();
        int remain = Math.max(0, target - currentQty);
        int percent = target == 0 ? 0 : Math.min(100, (int) Math.round(currentQty * 100.0 / target));
        BigDecimal saving = gb.getProduct().getPrice().subtract(gb.getGroupPrice());

        return GroupBuyResponse.builder()
                .id(gb.getId())
                .productId(gb.getProduct().getId())
                .productName(gb.getProduct().getName())
                .productImageUrl(gb.getProduct().getImageUrl())
                .productUnit(gb.getProduct().getUnit())
                .productPrice(gb.getProduct().getPrice())
                .farmerId(gb.getFarmer().getId())
                .farmerName(gb.getFarmer().getName())
                .hostId(gb.getHost().getId())
                .hostName(gb.getHost().getName())
                .targetQuantity(target)
                .currentQuantity(currentQty)
                .remainingQuantity(remain)
                .percent(percent)
                .groupPrice(gb.getGroupPrice())
                .saving(saving)
                .openDate(gb.getOpenDate())
                .deadlineDate(gb.getDeadlineDate())
                .status(gb.getStatus())
                .createdAt(gb.getCreatedAt())
                .joined(joined)
                .myQuantity(myQty)
                .build();
    }
}
