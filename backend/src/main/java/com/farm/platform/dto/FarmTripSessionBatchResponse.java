package com.farm.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class FarmTripSessionBatchResponse {
    /** 實際新建的場次 */
    private List<FarmTripSessionResponse> created;
    /** 因衝突或已存在而略過的日期 (yyyy-MM-dd 字串) */
    private List<String> skipped;
}
