package com.sd.backend.service;

import com.sd.backend.dto.ReminderRequest;
import com.sd.backend.dto.ReminderResponse;
import com.sd.backend.dto.ReminderUpdateRequest;
import com.sd.backend.exception.ResourceNotFoundException;
import com.sd.backend.exception.UnauthorizedException;
import com.sd.backend.model.Reminder;
import com.sd.backend.model.User;
import com.sd.backend.model.enums.ReminderType;
import com.sd.backend.repository.ReminderRepository;
import com.sd.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReminderService {
    
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public List<ReminderResponse> getReminders(UUID userId, ReminderType type, Boolean isRead) {
        List<Reminder> reminders;
        
        if (type != null) {
            reminders = reminderRepository.findByUserIdAndType(userId, type);
        } else if (isRead != null) {
            reminders = reminderRepository.findByUserIdAndIsRead(userId, isRead);
        } else {
            reminders = reminderRepository.findByUserId(userId);
        }
        
        return reminders.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public ReminderResponse createReminder(ReminderRequest request, UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Reminder reminder = new Reminder();
        reminder.setUser(user);
        reminder.setType(request.getType());
        reminder.setTitle(request.getTitle());
        reminder.setMessage(request.getMessage());
        reminder.setScheduledAt(request.getScheduledAt());
        reminder.setIsRead(false);
        
        reminder = reminderRepository.save(reminder);
        return toResponse(reminder);
    }
    
    @Transactional
    public ReminderResponse updateReminder(UUID id, ReminderUpdateRequest request, UUID userId) {
        Reminder reminder = reminderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));
        
        if (!reminder.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized access to reminder");
        }
        
        if (request.getTitle() != null) {
            reminder.setTitle(request.getTitle());
        }
        
        if (request.getMessage() != null) {
            reminder.setMessage(request.getMessage());
        }
        
        if (request.getScheduledAt() != null) {
            reminder.setScheduledAt(request.getScheduledAt());
        }
        
        reminder = reminderRepository.save(reminder);
        return toResponse(reminder);
    }
    
    @Transactional
    public void markAsRead(UUID id, UUID userId) {
        Reminder reminder = reminderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));
        
        if (!reminder.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized access to reminder");
        }
        
        reminder.setIsRead(true);
        reminderRepository.save(reminder);
    }
    
    @Transactional
    public void deleteReminder(UUID id, UUID userId) {
        Reminder reminder = reminderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));
        
        if (!reminder.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized access to reminder");
        }
        
        reminderRepository.delete(reminder);
    }
    
    private ReminderResponse toResponse(Reminder reminder) {
        return new ReminderResponse(
            reminder.getId(),
            reminder.getUser().getId(),
            reminder.getType(),
            reminder.getTitle(),
            reminder.getMessage(),
            reminder.getScheduledAt(),
            reminder.getSentAt(),
            reminder.getIsRead(),
            reminder.getMetadata(),
            reminder.getCreatedAt(),
            reminder.getUpdatedAt()
        );
    }
}
