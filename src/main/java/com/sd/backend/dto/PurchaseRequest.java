package com.sd.backend.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class PurchaseRequest {
    @NotBlank
    private String purchaseToken;

    @NotBlank
    private String productId;
}
