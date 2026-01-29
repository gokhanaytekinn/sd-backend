package com.sd.backend.controller;

import com.sd.backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
    @GetMapping("/subscriptions")
    public ResponseEntity<Map<String, Object>> getSubscriptionMetrics() {
        Map<String, Object> metrics = analyticsService.getSubscriptionMetrics();
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueMetrics() {
        Map<String, Object> metrics = analyticsService.getRevenueMetrics();
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/engagement")
    public ResponseEntity<Map<String, Object>> getUserEngagementMetrics() {
        Map<String, Object> metrics = analyticsService.getUserEngagementMetrics();
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/conversions")
    public ResponseEntity<Map<String, Object>> getConversionMetrics() {
        Map<String, Object> metrics = analyticsService.getConversionMetrics();
        return ResponseEntity.ok(metrics);
    }
}
