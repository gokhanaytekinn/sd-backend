package com.sd.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private int status;              // HTTP status code
    private String errorCode;        // Error type code for frontend (e.g., "AUTH_001")
    private String message;          // Detailed message for developers
    private String userMessage;      // User-friendly message in Turkish
    private String path;             // Request path where error occurred
    private LocalDateTime timestamp;
}
