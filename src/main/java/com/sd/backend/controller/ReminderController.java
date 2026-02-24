package com.sd.backend.controller;

import com.sd.backend.dto.ReminderRequest;
import com.sd.backend.dto.ReminderResponse;
import com.sd.backend.dto.ReminderUpdateRequest;
import com.sd.backend.model.enums.ReminderType;
import com.sd.backend.service.ReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping
    public ResponseEntity<List<ReminderResponse>> getReminders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) ReminderType type,
            @RequestParam(required = false) Boolean isRead) {
        String userId = userDetails.getUsername();
        List<ReminderResponse> reminders = reminderService.getReminders(userId, type, isRead);
        return ResponseEntity.ok(reminders);
    }

    @PostMapping
    public ResponseEntity<ReminderResponse> createReminder(
            @Valid @RequestBody ReminderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        ReminderResponse reminder = reminderService.createReminder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(reminder);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReminderResponse> updateReminder(
            @PathVariable("id") String id,
            @Valid @RequestBody ReminderUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        ReminderResponse reminder = reminderService.updateReminder(id, request, userId);
        return ResponseEntity.ok(reminder);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        reminderService.markAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminder(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        reminderService.deleteReminder(id, userId);
        return ResponseEntity.noContent().build();
    }
}
