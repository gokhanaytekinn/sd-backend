package com.sd.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationSettingsRequest {
    @NotNull
    private Boolean enabled;

    private String language;
}
