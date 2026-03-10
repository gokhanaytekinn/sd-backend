package com.sd.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventDTO {
    private String subscriptionId;
    private String subscriptionName;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String icon;
}
