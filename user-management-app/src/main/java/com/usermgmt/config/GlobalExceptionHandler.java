package com.usermgmt.config;

import com.usermgmt.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Fix: Khi @Valid thất bại trên REST API, trả về 400 Bad Request thay vì 302 redirect.
 *
 * Nguyên nhân lỗi: Spring Security intercepted validation failures và redirect về /login
 * vì không có handler cho MethodArgumentNotValidException trên /api/** endpoints.
 *
 * Tests được fix: testRegisterInvalidEmail, testRegisterMissingFields, testRegisterShortPassword
 */
@RestControllerAdvice(basePackages = "com.usermgmt.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        String firstError = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Validation failed");

        return ResponseEntity.badRequest()
                .body(UserDto.MessageResponse.builder()
                        .message(firstError)
                        .success(false)
                        .build());
    }
}