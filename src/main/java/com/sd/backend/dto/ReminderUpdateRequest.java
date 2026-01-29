package com.sd.backend.dto;

import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReminderUpdateRequest {
    private String title;
    private String message;
    
    @Future(message = "Scheduled time must be in the future")
    private LocalDateTime scheduledAt;
}
