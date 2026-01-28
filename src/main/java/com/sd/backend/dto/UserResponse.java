package com.sd.backend.dto;

import com.sd.backend.model.enums.UserTier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String name;
    private UserTier tier;
    private LocalDateTime createdAt;
}
