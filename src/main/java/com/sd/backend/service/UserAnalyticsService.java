package com.sd.backend.service;

import com.sd.backend.dto.CalendarEventDTO;
import com.sd.backend.dto.UserAnalyticsInsightResponse;
import com.sd.backend.dto.UserAnalyticsSummaryResponse;
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
    public UserAnalyticsSummaryResponse getSummary(String userId, String category) {
        List<Subscription> allSubscriptions = subscriptionRepository.findByUserIdOrJointUserIdsContaining(userId, userId)
                .stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .collect(Collectors.toList());

        List<Subscription> filteredSubscriptions = allSubscriptions;
        if (category != null && !category.equalsIgnoreCase("All")) {
            filteredSubscriptions = allSubscriptions.stream()
                    .filter(s -> category.equalsIgnoreCase(s.getCategory()))
                    .collect(Collectors.toList());
        }

        BigDecimal totalMonthly = BigDecimal.ZERO;
        Map<String, BigDecimal> categoryBreakdown = new HashMap<>();
        Map<String, UserAnalyticsSummaryResponse.LifetimeMetric> lifetimeSpent = new HashMap<>();
        List<CalendarEventDTO> calendarEvents = new ArrayList<>();
        LocalDate now = LocalDate.now();

        // Calculate metrics for filtered subscriptions
        for (Subscription sub : filteredSubscriptions) {
            BigDecimal monthlyCost = calculateMonthlyCost(sub);
            BigDecimal normalizedMonthlyCost = convertToBaseCurrency(monthlyCost, sub.getCurrency());
            
            totalMonthly = totalMonthly.add(normalizedMonthlyCost);
            
            // Category breakdown (only truly useful if category is All)
            String subCategory = sub.getCategory() != null ? sub.getCategory() : "category_other";
            categoryBreakdown.put(subCategory, categoryBreakdown.getOrDefault(subCategory, BigDecimal.ZERO).add(normalizedMonthlyCost));

            // Lifetime spent (estimate)
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
        }

        // Generate calendar events for ALL active subscriptions (or filtered depending on preference? 
        // User said: "o seçtiği kategoriye göre aşağıdaki grafikler hazırlansın... abonelik yenileme tarihlerinde mavi nokta olsun")
        // It's probably better to show EVERYTHING on calendar unless the filter specifically should apply to calendar too.
        // Given the request, I'll filter the calendar too.
        for (Subscription sub : filteredSubscriptions) {
            addAllRenewalDatesForYear(sub, calendarEvents);
        }

        return UserAnalyticsSummaryResponse.builder()
                .totalMonthlyCost(totalMonthly.setScale(2, RoundingMode.HALF_UP))
                .totalYearlyCost(totalMonthly.multiply(new BigDecimal("12")).setScale(2, RoundingMode.HALF_UP))
                .calendarEvents(calendarEvents)
                .lifetimeSpent(lifetimeSpent)
                .categoryBreakdown(categoryBreakdown)
                .currency(CurrencyCode.TRY)
                .build();
    }

    private void addAllRenewalDatesForYear(Subscription sub, List<CalendarEventDTO> events) {
        if (sub.getBillingDay() == null) return;
        
        LocalDate now = LocalDate.now();
        LocalDate endOfYear = now.plusYears(1);
        
        // This is a simplified renewal calculator
        if (sub.getBillingCycle() == BillingCycle.MONTHLY) {
            for (int i = 0; i <= 12; i++) {
                LocalDate date = now.plusMonths(i).withDayOfMonth(Math.min(sub.getBillingDay(), now.plusMonths(i).lengthOfMonth()));
                if (!date.isBefore(now)) {
                    events.add(createEvent(sub, date));
                }
            }
        } else if (sub.getBillingCycle() == BillingCycle.YEARLY && sub.getBillingMonth() != null) {
            LocalDate date = LocalDate.of(now.getYear(), sub.getBillingMonth(), Math.min(sub.getBillingDay(), LocalDate.of(now.getYear(), sub.getBillingMonth(), 1).lengthOfMonth()));
            if (date.isBefore(now)) date = date.plusYears(1);
            events.add(createEvent(sub, date));
        }
    }

    private CalendarEventDTO createEvent(Subscription sub, LocalDate date) {
        return CalendarEventDTO.builder()
                .subscriptionId(sub.getId())
                .subscriptionName(sub.getName())
                .amount(convertToBaseCurrency(sub.getAmount(), sub.getCurrency()))
                .paymentDate(date)
                .icon(sub.getIcon())
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
        UserAnalyticsSummaryResponse summary = getSummary(userId, null);
        summary.getCategoryBreakdown().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> {
                    if (entry.getValue().compareTo(summary.getTotalMonthlyCost().multiply(new BigDecimal("0.5"))) > 0) {
                        insights.add("insight_category_dominant:" + entry.getKey());
                    }
                });

        // Insight 3: Duplicate Categories (Savings Potential)
        Map<String, Long> categoryCounts = subscriptions.stream()
                .collect(Collectors.groupingBy(Subscription::getCategory, Collectors.counting()));
        categoryCounts.forEach((cat, count) -> {
            if (count >= 2) {
                insights.add("insight_saving_potential:" + cat);
            }
        });

        // Insight 4: Cycle Suggestion (Long term monthly)
        // For demo, if a user has many monthly (>4), suggest yearly
        if (monthlyCount > 4) {
             insights.add("insight_cycle_suggestion");
        }

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
