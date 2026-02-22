package com.sd.backend.service;

import com.sd.backend.model.Subscription;
import com.sd.backend.model.Transaction;
import com.sd.backend.model.User;
import com.sd.backend.model.enums.SubscriptionStatus;
import com.sd.backend.model.enums.TransactionStatus;
import com.sd.backend.model.enums.TransactionType;
import com.sd.backend.model.enums.UserTier;
import com.sd.backend.repository.SubscriptionRepository;
import com.sd.backend.repository.TransactionRepository;
import com.sd.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final SubscriptionRepository subscriptionRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getSubscriptionMetrics() {
        List<Subscription> allSubscriptions = subscriptionRepository.findAll();

        long totalCount = allSubscriptions.size();
        long activeCount = allSubscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .count();
        long suspendedCount = allSubscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.SUSPENDED)
                .count();
        long cancelledCount = allSubscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.CANCELLED)
                .count();
        long pendingApprovalCount = allSubscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.PENDING_APPROVAL)
                .count();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalSubscriptions", totalCount);
        metrics.put("activeSubscriptions", activeCount);
        metrics.put("suspendedSubscriptions", suspendedCount);
        metrics.put("cancelledSubscriptions", cancelledCount);
        metrics.put("pendingApprovalSubscriptions", pendingApprovalCount);

        return metrics;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRevenueMetrics() {
        List<Transaction> completedTransactions = transactionRepository
                .findByStatusAndTypeIn(TransactionStatus.COMPLETED,
                        List.of(TransactionType.SUBSCRIPTION_PAYMENT, TransactionType.UPGRADE));

        BigDecimal totalRevenue = completedTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageRevenue = completedTransactions.isEmpty()
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(completedTransactions.size()), 2, RoundingMode.HALF_UP);

        Map<YearMonth, BigDecimal> revenueByMonth = new HashMap<>();
        for (Transaction transaction : completedTransactions) {
            YearMonth month = YearMonth.from(transaction.getCreatedAt());
            revenueByMonth.merge(month, transaction.getAmount(), BigDecimal::add);
        }

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalRevenue", totalRevenue);
        metrics.put("averageRevenue", averageRevenue);
        metrics.put("transactionCount", completedTransactions.size());
        metrics.put("revenueByMonth", revenueByMonth);

        return metrics;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserEngagementMetrics() {
        List<User> allUsers = userRepository.findAll();

        long totalUsers = allUsers.size();
        long freeUsers = allUsers.stream()
                .filter(u -> u.getTier() == UserTier.FREE)
                .count();
        long premiumUsers = allUsers.stream()
                .filter(u -> u.getTier() == UserTier.PREMIUM)
                .count();

        double conversionRate = totalUsers == 0
                ? 0.0
                : (double) premiumUsers / totalUsers * 100;

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalUsers", totalUsers);
        metrics.put("freeUsers", freeUsers);
        metrics.put("premiumUsers", premiumUsers);
        metrics.put("conversionRate", String.format("%.2f%%", conversionRate));

        return metrics;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getConversionMetrics() {
        List<Transaction> upgradeTransactions = transactionRepository
                .findByStatusAndTypeIn(TransactionStatus.COMPLETED, List.of(TransactionType.UPGRADE));

        long totalConversions = upgradeTransactions.size();

        Map<YearMonth, Long> conversionsByMonth = new HashMap<>();
        for (Transaction transaction : upgradeTransactions) {
            YearMonth month = YearMonth.from(transaction.getCreatedAt());
            conversionsByMonth.merge(month, 1L, (a, b) -> a + b);
        }

        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        long premiumUsers = allUsers.stream()
                .filter(u -> u.getTier() == UserTier.PREMIUM)
                .count();

        double conversionRate = totalUsers == 0
                ? 0.0
                : (double) premiumUsers / totalUsers * 100;

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalConversions", totalConversions);
        metrics.put("conversionsByMonth", conversionsByMonth);
        metrics.put("overallConversionRate", String.format("%.2f%%", conversionRate));

        return metrics;
    }
}
