package com.sd.backend.controller;

import com.sd.backend.dto.UserAnalyticsInsightResponse;
import com.sd.backend.dto.UserAnalyticsSummaryResponse;
import com.sd.backend.dto.UserAnalyticsTrendResponse;
import com.sd.backend.service.UserAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-analytics")
@RequiredArgsConstructor
@Tag(name = "User Analytics", description = "Personalized spending analytics and insights")
@SecurityRequirement(name = "bearerAuth")
public class UserAnalyticsController {

    private final UserAnalyticsService userAnalyticsService;

    @GetMapping("/summary")
    @Operation(summary = "Get personal spending summary", description = "Get normalized monthly and yearly spending summary for the authenticated user, optionally filtered by category")
    public ResponseEntity<UserAnalyticsSummaryResponse> getSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String category) {
        String userId = userDetails.getUsername();
        return ResponseEntity.ok(userAnalyticsService.getSummary(userId, category));
    }

    @GetMapping("/trends")
    @Operation(summary = "Get spending trends", description = "Get spending trends for the last 6 months for the authenticated user")
    public ResponseEntity<UserAnalyticsTrendResponse> getTrends(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        return ResponseEntity.ok(userAnalyticsService.getTrends(userId));
    }

    @GetMapping("/insights")
    @Operation(summary = "Get smart insights", description = "Get personalized tips and insights about spending habits")
    public ResponseEntity<UserAnalyticsInsightResponse> getInsights(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        return ResponseEntity.ok(userAnalyticsService.getInsights(userId));
    }
}
