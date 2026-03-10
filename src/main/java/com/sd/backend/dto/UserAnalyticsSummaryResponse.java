package com.sd.backend.dto;

import com.sd.backend.model.enums.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnalyticsSummaryResponse {
    private BigDecimal totalMonthlyCost;
    private BigDecimal totalYearlyCost;
    private Map<String, BigDecimal> categoryBreakdown;
    private CurrencyCode currency;
}
