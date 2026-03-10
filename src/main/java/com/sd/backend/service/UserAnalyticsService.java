package com.sd.backend.service;

import com.sd.backend.dto.UpcomingPaymentDTO;
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
import java.time.LocalDate;
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
        Map<String, UserAnalyticsSummaryResponse.LifetimeMetric> lifetimeSpent = new HashMap<>();
        List<UpcomingPaymentDTO> upcomingPayments = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (Subscription sub : subscriptions) {
            BigDecimal monthlyCost = calculateMonthlyCost(sub);
            BigDecimal normalizedMonthlyCost = convertToBaseCurrency(monthlyCost, sub.getCurrency());
            
            totalMonthly = totalMonthly.add(normalizedMonthlyCost);
            
            // Category breakdown
            String category = sub.getCategory() != null ? sub.getCategory() : "Other";
            categoryBreakdown.put(category, categoryBreakdown.getOrDefault(category, BigDecimal.ZERO).add(normalizedMonthlyCost));

            // Lifetime spent (estimate if no transactions)
            if (sub.getCreatedAt() != null) {
                long daysAlive = java.time.temporal.ChronoUnit.DAYS.between(sub.getCreatedAt().toLocalDate(), now);
                BigDecimal monthsAlive = new BigDecimal(Math.max(1, daysAlive)).divide(new BigDecimal("30"), 2, RoundingMode.HALF_UP);
                BigDecimal totalSpent = normalizedMonthlyCost.multiply(monthsAlive).setScale(2, RoundingMode.HALF_UP);
                lifetimeSpent.put(sub.getId(), UserAnalyticsSummaryResponse.LifetimeMetric.builder()
                        .name(sub.getName())
                        .totalAmount(totalSpent)
                        .icon(sub.getIcon())
                        .build());
            }

            // Upcoming payment (next 30 days)
            LocalDate nextRenewal = sub.getNextRenewalDate();
            if (nextRenewal != null && nextRenewal.isBefore(now.plusDays(31))) {
                upcomingPayments.add(UpcomingPaymentDTO.builder()
                        .subscriptionId(sub.getId())
                        .subscriptionName(sub.getName())
                        .amount(convertToBaseCurrency(sub.getAmount(), sub.getCurrency()))
                        .paymentDate(nextRenewal)
                        .icon(sub.getIcon())
                        .build());
            }
        }

        // Sort upcoming payments by date
        upcomingPayments.sort(java.util.Comparator.comparing(UpcomingPaymentDTO::getPaymentDate));

        return UserAnalyticsSummaryResponse.builder()
                .totalMonthlyCost(totalMonthly.setScale(2, RoundingMode.HALF_UP))
                .totalYearlyCost(totalMonthly.multiply(new BigDecimal("12")).setScale(2, RoundingMode.HALF_UP))
                .dailyAverageCost(totalMonthly.divide(new BigDecimal("30"), 2, RoundingMode.HALF_UP))
                .upcomingPayments(upcomingPayments)
                .lifetimeSpent(lifetimeSpent)
                .categoryBreakdown(categoryBreakdown)
                .currency(CurrencyCode.TRY)
                .build();
    }

    @Transactional(readOnly = true)
    public UserAnalyticsTrendResponse getTrends(String userId) {
        List<Subscription> subscriptions = subscriptionRepository.findByUserIdOrJointUserIdsContaining(userId, userId)
                .stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .collect(Collectors.toList());

        List<UserAnalyticsTrendResponse.MonthTrend> trends = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate targetMonth = now.minusMonths(i);
            String monthLabel = targetMonth.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH);
            
            BigDecimal monthlyTotal = BigDecimal.ZERO;
            for (Subscription sub : subscriptions) {
                // Check if subscription existed in that month
                if (sub.getCreatedAt() != null && sub.getCreatedAt().toLocalDate().isBefore(targetMonth.withDayOfMonth(targetMonth.lengthOfMonth()).plusDays(1))) {
                    BigDecimal monthlyCost = calculateMonthlyCost(sub);
                    BigDecimal normalizedCost = convertToBaseCurrency(monthlyCost, sub.getCurrency());
                    monthlyTotal = monthlyTotal.add(normalizedCost);
                }
            }
            trends.add(new UserAnalyticsTrendResponse.MonthTrend(monthLabel, monthlyTotal.setScale(2, RoundingMode.HALF_UP)));
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
            insights.add("insight_yearly_optimization");
        }

        // Insight 2: High cost category
        UserAnalyticsSummaryResponse summary = getSummary(userId);
        summary.getCategoryBreakdown().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> {
                    if (entry.getValue().compareTo(summary.getTotalMonthlyCost().multiply(new BigDecimal("0.5"))) > 0) {
                        insights.add("insight_category_dominant:" + entry.getKey());
                    }
                });

        if (insights.isEmpty()) {
            insights.add("insight_balanced_spending");
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
