package com.sd.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sd.backend.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Wrap request and response to cache content
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            // Continue filter chain
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            logStructured(wrappedRequest, wrappedResponse);
            // Copy cached response content to actual response
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logStructured(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        try {
            Map<String, Object> logObj = new LinkedHashMap<>();

            // User identity from SecurityContextHolder
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String user = "anonymous";
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                if (principal instanceof UserPrincipal) {
                    user = ((UserPrincipal) principal).getFullNameWithEmail();
                } else {
                    user = auth.getName();
                }
            }
            logObj.put("user", user);

            // Full service endpoint including query string
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            logObj.put("service", uri + (queryString != null ? "?" + queryString : ""));

            // Full request URL
            logObj.put("url", request.getRequestURL().toString() + (queryString != null ? "?" + queryString : ""));

            // Extract Request Body
            Object requestBody = null;
            byte[] requestContent = request.getContentAsByteArray();
            if (requestContent.length > 0) {
                String bodyStr = new String(requestContent, StandardCharsets.UTF_8);
                try {
                    requestBody = objectMapper.readValue(bodyStr, Object.class);
                } catch (Exception e) {
                    requestBody = bodyStr;
                }
            }
            logObj.put("request", requestBody);

            // Extract Params (Query Parameters and Form Data)
            Map<String, Object> paramsObj = null;
            if (!request.getParameterMap().isEmpty()) {
                paramsObj = new LinkedHashMap<>();
                for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                    String[] values = entry.getValue();
                    if (values.length == 1) {
                        paramsObj.put(entry.getKey(), values[0]);
                    } else {
                        paramsObj.put(entry.getKey(), values);
                    }
                }
            }
            logObj.put("params", paramsObj);

            // Extract Response Object
            Object responseObj = null;
            byte[] responseContent = response.getContentAsByteArray();
            if (responseContent.length > 0) {
                String bodyStr = new String(responseContent, StandardCharsets.UTF_8);
                try {
                    responseObj = objectMapper.readValue(bodyStr, Object.class);
                } catch (Exception e) {
                    responseObj = bodyStr;
                }
            }
            logObj.put("response", responseObj);

            log.info(objectMapper.writeValueAsString(logObj));
        } catch (Exception e) {
            log.error("Error logging structured request/response", e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Don't log static resources or actuator endpoints
        String path = request.getRequestURI();
        return path.startsWith("/actuator") ||
                path.startsWith("/swagger") ||
                path.startsWith("/v3/api-docs") ||
                path.contains(".") || // static files
                path.equals("/") ||
                path.equals("/favicon.ico");
    }
}
