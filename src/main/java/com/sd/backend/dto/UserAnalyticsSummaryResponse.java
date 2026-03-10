package com.sd.backend.dto;

import com.sd.backend.model.enums.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnalyticsSummaryResponse {
    private BigDecimal totalMonthlyCost;
    private BigDecimal totalYearlyCost;
    private BigDecimal dailyAverageCost;
    private Map<String, LifetimeMetric> lifetimeSpent;
    private Map<String, BigDecimal> categoryBreakdown;
    private List<CalendarEventDTO> calendarEvents;
    private CurrencyCode currency;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LifetimeMetric {
        private String name;
        private BigDecimal totalAmount;
        private String icon;
    }
}
