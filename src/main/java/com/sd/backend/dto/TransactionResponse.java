package com.sd.backend.dto;

import com.sd.backend.model.enums.TransactionStatus;
import com.sd.backend.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private UUID id;
    private UUID userId;
    private UUID subscriptionId;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
