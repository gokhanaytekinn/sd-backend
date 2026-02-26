package com.sd.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    private String getMessage(String code, String defaultMessage) {
        return messageSource.getMessage(code, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    private String mapExceptionMessageToKey(String message) {
        if (message == null)
            return null;
        return switch (message) {
            case "Email already exists" -> "error.email.exists";
            case "Invalid credentials" -> "error.invalid.credentials";
            case "Invalid Google ID token" -> "error.invalid.google.token";
            case "Unauthorized access to subscription" -> "error.unauthorized.subscription";
            case "User not found" -> "error.user.not.found";
            case "Subscription not found" -> "error.subscription.not.found";
            case "Invalid reset code" -> "error.invalid.reset.code";
            case "Reset code has expired" -> "error.expired.reset.code";

            // Validation Messages
            case "Email is required" -> "validation.email.required";
            case "Email must be valid", "Invalid email format" -> "validation.email.invalid";
            case "Password is required" -> "validation.password.required";
            case "Password must be at least 6 characters" -> "validation.password.length";
            case "Code is required" -> "validation.code.required";
            case "Code must be 6 digits" -> "validation.code.length";
            case "New password is required" -> "validation.new_password.required";
            case "Name is required" -> "validation.name.required";
            case "Type is required" -> "validation.type.required";
            case "Amount is required" -> "validation.amount.required";
            case "Amount must be greater than 0" -> "validation.amount.min";
            case "Currency is required" -> "validation.currency.required";
            case "Currency must be 3 characters" -> "validation.currency.length";
            case "Subscription name is required" -> "validation.subscription.name.required";
            case "Billing cycle is required" -> "validation.billing_cycle.required";
            case "Start date is required" -> "validation.start_date.required";
            case "Title is required" -> "validation.title.required";
            case "Message is required" -> "validation.message.required";
            case "Scheduled time is required" -> "validation.scheduled_time.required";
            case "Scheduled time must be in the future" -> "validation.scheduled_time.future";
            case "idToken is required" -> "validation.id_token.required";
            case "Reason is required" -> "validation.reason.required";
            case "Token is required" -> "validation.token.required";

            default -> {
                if (message.startsWith("User not found with email"))
                    yield "error.user.not.found";
                if (message.startsWith("Google token verification failed"))
                    yield "error.google.verification.failed";
                yield null;
            }
        };
    }

    private String getLocalizedExceptionMessage(String originalMessage, String fallbackCode, String fallbackDefault) {
        String key = mapExceptionMessageToKey(originalMessage);
        if (key != null) {
            return getMessage(key, originalMessage);
        }
        return getMessage(fallbackCode, fallbackDefault);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        String localizedMessage = getLocalizedExceptionMessage(ex.getMessage(), "error.not_found", ex.getMessage());
        String localizedDetail = getMessage("error.not_found.detail",
                "The record you are looking for could not be found");

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND_001",
                localizedMessage,
                localizedDetail,
                request.getRequestURI(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex,
            HttpServletRequest request) {

        String localizedMessage = getLocalizedExceptionMessage(ex.getMessage(), "error.unauthorized", ex.getMessage());
        String localizedDetail = getMessage("error.unauthorized.detail",
                "Incorrect email or password. Please try again.");

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "AUTH_001",
                localizedMessage,
                localizedDetail,
                request.getRequestURI(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException ex,
            HttpServletRequest request) {

        String localizedMessage = getLocalizedExceptionMessage(ex.getMessage(), "error.bad_request", ex.getMessage());
        String localizedDetail = getMessage("error.bad_request.detail",
                "An error occurred while processing the request");

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST_001",
                localizedMessage,
                localizedDetail,
                request.getRequestURI(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> getLocalizedExceptionMessage(error.getDefaultMessage(), "error.validation",
                        error.getDefaultMessage()))
                .collect(Collectors.joining(", "));

        String localizedDetail = getMessage("error.validation.detail", "Please fill in all required fields correctly");

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_001",
                errorMessage,
                localizedDetail,
                request.getRequestURI(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex,
            HttpServletRequest request) {

        String localizedMessage = getMessage("error.server", "Internal Server Error");
        String localizedDetail = getMessage("error.server.detail",
                "An unexpected error occurred. Please try again later.");

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "SERVER_001",
                localizedMessage + ": " + ex.getMessage(),
                localizedDetail,
                request.getRequestURI(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
