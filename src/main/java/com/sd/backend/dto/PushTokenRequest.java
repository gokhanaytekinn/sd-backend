package com.sd.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PushTokenRequest {
    @NotBlank
    private String token;
    
    @NotBlank
    private String platform; // "android" or "ios"
}
