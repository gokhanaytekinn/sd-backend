package com.sd.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Wrap request and response to cache content
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        
        long startTime = System.currentTimeMillis();
        
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = uri + (queryString != null ? "?" + queryString : "");
        
        // Log incoming request
        log.info("==> {} {}", method, fullPath);
        
        try {
            // Continue filter chain
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            // Calculate duration
            long duration = System.currentTimeMillis() - startTime;
            
            // Log request body for POST/PUT/PATCH (but not for multipart/form-data)
            if (shouldLogRequestBody(method, request.getContentType())) {
                byte[] content = wrappedRequest.getContentAsByteArray();
                if (content.length > 0) {
                    String body = new String(content, StandardCharsets.UTF_8);
                    // Limit body length to prevent excessive logging
                    String logBody = body.length() > 500 ? body.substring(0, 500) + "..." : body;
                    log.info("    Request Body: {}", logBody);
                }
            }
            
            // Log response
            log.info("<== {} {} - Status: {} - {}ms", 
                    method, uri, wrappedResponse.getStatus(), duration);
            
            // Copy cached response content to actual response
            wrappedResponse.copyBodyToResponse();
        }
    }
    
    private boolean shouldLogRequestBody(String method, String contentType) {
        // Log body for POST, PUT, PATCH
        if (!method.equals("POST") && !method.equals("PUT") && !method.equals("PATCH")) {
            return false;
        }
        
        // Don't log multipart/form-data (file uploads)
        if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
            return false;
        }
        
        return true;
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Don't log static resources or actuator endpoints
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || 
               path.startsWith("/swagger") || 
               path.startsWith("/v3/api-docs");
    }
}
