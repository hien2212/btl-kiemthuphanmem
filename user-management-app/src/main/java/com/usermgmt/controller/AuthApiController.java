package com.usermgmt.controller;

import com.usermgmt.dto.UserDto;
import com.usermgmt.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDto.RegisterRequest request) {
        try {
            UserDto.UserResponse user = userService.register(request);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(UserDto.MessageResponse.builder()
                            .message(e.getMessage())
                            .success(false)
                            .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserDto.LoginRequest request) {
        try {
            UserDto.AuthResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(UserDto.MessageResponse.builder()
                            .message("Invalid username or password")
                            .success(false)
                            .build());
        }
    }
}
