package com.sd.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppleSignInRequest {
    @NotBlank(message = "Identity token is required")
    private String identityToken;
    
    private String firstName;
    private String lastName;
}
