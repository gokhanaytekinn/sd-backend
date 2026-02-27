package com.sd.backend.dto;

import com.sd.backend.model.enums.UserTier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String name;
    private UserTier tier;
    private Boolean notificationsEnabled;
    private String language;
    private LocalDateTime createdAt;
}
