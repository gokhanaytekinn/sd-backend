package com.sd.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnalyticsTrendResponse {
    private List<MonthTrend> monthlyTrends;

    @Data
    @AllArgsConstructor
    public static class MonthTrend {
        private String month;
        private BigDecimal totalCost;
    }
}
