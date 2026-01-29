package com.sd.backend.dto;

import com.sd.backend.model.enums.ReminderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReminderResponse {
    private String id;
    private String userId;
    private ReminderType type;
    private String title;
    private String message;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private Boolean isRead;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
