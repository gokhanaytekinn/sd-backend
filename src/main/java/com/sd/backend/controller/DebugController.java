package com.sd.backend.controller;

import com.sd.backend.model.Subscription;
import com.sd.backend.model.enums.SubscriptionStatus;
import com.sd.backend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final SubscriptionRepository subscriptionRepository;
    private final com.sd.backend.repository.ReminderRepository reminderRepository;

    @GetMapping("/subscriptions/tomorrow")
    public List<Subscription> getTomorrowSubscriptions() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return subscriptionRepository.findByStatusAndReminderEnabled(SubscriptionStatus.ACTIVE, true)
                .stream()
                .filter(sub -> {
                    LocalDate next = sub.getNextRenewalDate();
                    return next != null && !next.isBefore(tomorrow) && next.isBefore(tomorrow.plusDays(1));
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/subscriptions/all")
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @GetMapping("/reminders/all")
    public List<com.sd.backend.model.Reminder> getAllReminders() {
        return reminderRepository.findAll();
    }
}
