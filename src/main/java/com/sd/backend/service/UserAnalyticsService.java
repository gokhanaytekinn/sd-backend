package com.sd.backend.service;

import com.sd.backend.dto.UserAnalyticsInsightResponse;
import com.sd.backend.dto.UserAnalyticsSummaryResponse;
import com.sd.backend.dto.UserAnalyticsTrendResponse;
import com.sd.backend.model.Subscription;
import com.sd.backend.model.enums.BillingCycle;
import com.sd.backend.model.enums.CurrencyCode;
import com.sd.backend.model.enums.SubscriptionStatus;
import com.sd.backend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAnalyticsService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public UserAnalyticsSummaryResponse getSummary(String userId) {
        List<Subscription> subscriptions = subscriptionRepository.findByUserIdOrJointUserIdsContaining(userId, userId)
                .stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .collect(Collectors.toList());

        BigDecimal totalMonthly = BigDecimal.ZERO;
        Map<String, BigDecimal> categoryBreakdown = new HashMap<>();

        for (Subscription sub : subscriptions) {
            BigDecimal monthlyCost = calculateMonthlyCost(sub);
            BigDecimal normalizedCost = convertToBaseCurrency(monthlyCost, sub.getCurrency());
            
            totalMonthly = totalMonthly.add(normalizedCost);
            
            String category = sub.getCategory() != null ? sub.getCategory() : "Other";
            categoryBreakdown.put(category, categoryBreakdown.getOrDefault(category, BigDecimal.ZERO).add(normalizedCost));
        }

        return UserAnalyticsSummaryResponse.builder()
                .totalMonthlyCost(totalMonthly.setScale(2, RoundingMode.HALF_UP))
                .totalYearlyCost(totalMonthly.multiply(new BigDecimal("12")).setScale(2, RoundingMode.HALF_UP))
                .categoryBreakdown(categoryBreakdown)
                .currency(CurrencyCode.TRY)
                .build();
    }

    @Transactional(readOnly = true)
    public UserAnalyticsTrendResponse getTrends(String userId) {
        // Simple mock for trends based on current subscriptions
        // In a real app, this would query a transaction history or historical snapshots
        UserAnalyticsSummaryResponse summary = getSummary(userId);
        List<UserAnalyticsTrendResponse.MonthTrend> trends = new ArrayList<>();
        
        // Mocking last 6 months with slight variations
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
        BigDecimal base = summary.getTotalMonthlyCost();
        
        for (int i = 0; i < 6; i++) {
            BigDecimal variation = new BigDecimal(0.9 + (Math.random() * 0.2)); // 90% - 110%
            trends.add(new UserAnalyticsTrendResponse.MonthTrend(months[i], base.multiply(variation).setScale(2, RoundingMode.HALF_UP)));
        }

        return UserAnalyticsTrendResponse.builder()
                .monthlyTrends(trends)
                .build();
    }

    @Transactional(readOnly = true)
    public UserAnalyticsInsightResponse getInsights(String userId) {
        List<Subscription> subscriptions = subscriptionRepository.findByUserIdOrJointUserIdsContaining(userId, userId)
                .stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .collect(Collectors.toList());

        List<String> insights = new ArrayList<>();
        
        // Insight 1: Yearly vs Monthly
        long monthlyCount = subscriptions.stream().filter(s -> s.getBillingCycle() == BillingCycle.MONTHLY).count();
        if (monthlyCount > 2) {
            insights.add("Bazı aylık aboneliklerinizi yıllık plana geçirerek yıllık bazda %15-20 tasarruf edebilirsiniz.");
        }

        // Insight 2: High cost category
        UserAnalyticsSummaryResponse summary = getSummary(userId);
        summary.getCategoryBreakdown().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> {
                    if (entry.getValue().compareTo(summary.getTotalMonthlyCost().multiply(new BigDecimal("0.5"))) > 0) {
                        insights.add(entry.getKey() + " kategorisi toplam harcamanızın yarısından fazlasını oluşturuyor.");
                    }
                });

        if (insights.isEmpty()) {
            insights.add("Harcamalarınız şu an dengeli görünüyor.");
        }

        return UserAnalyticsInsightResponse.builder()
                .insights(insights)
                .build();
    }

    private BigDecimal calculateMonthlyCost(Subscription sub) {
        BigDecimal amount = sub.getAmount() != null ? sub.getAmount() : BigDecimal.ZERO;
        if (sub.getBillingCycle() == null) return amount;

        switch (sub.getBillingCycle()) {
            case MONTHLY: return amount;
            case YEARLY: return amount.divide(new BigDecimal("12"), 4, RoundingMode.HALF_UP);
            case WEEKLY: return amount.multiply(new BigDecimal("4"));
            case QUARTERLY: return amount.divide(new BigDecimal("3"), 4, RoundingMode.HALF_UP);
            default: return amount;
        }
    }

    private BigDecimal convertToBaseCurrency(BigDecimal amount, CurrencyCode code) {
        if (code == null || code == CurrencyCode.TRY) return amount;
        
        // Using static estimated rates for demonstration
        BigDecimal rate = BigDecimal.ONE;
        switch (code) {
            case USD: rate = new BigDecimal("32.0"); break;
            case EUR: rate = new BigDecimal("35.0"); break;
            case GBP: rate = new BigDecimal("40.0"); break;
            case RUB: rate = new BigDecimal("0.35"); break;
            case AZN: rate = new BigDecimal("19.0"); break;
            case KZT: rate = new BigDecimal("0.07"); break;
        }
        return amount.multiply(rate);
    }
}
