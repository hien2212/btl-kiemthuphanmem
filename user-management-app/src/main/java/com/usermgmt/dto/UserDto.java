package com.usermgmt.dto;

import com.usermgmt.entity.User;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

public class UserDto {

    @Getter @Setter
    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, underscores")
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotBlank(message = "Please confirm your password")
        private String confirmPassword;
    }

    @Getter @Setter
    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String type = "Bearer";
        private Long id;
        private String username;
        private String email;
        private String role;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String role;
        private boolean enabled;
        private LocalDateTime createdAt;

        public static UserResponse from(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .enabled(user.isEnabled())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
    }

    @Getter @Setter
    public static class UpdateRoleRequest {
        @NotNull(message = "Role is required")
        private String role;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MessageResponse {
        private String message;
        private boolean success;
    }
}
