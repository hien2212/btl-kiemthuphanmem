package com.usermgmt.controller;

import com.usermgmt.dto.UserDto;
import com.usermgmt.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;

    // GET /api/users - Admin only
    @GetMapping("/admin/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UserDto.UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET /api/admin/users/{id}
    @GetMapping("/admin/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /api/admin/users/{id}/role - Change user role
    @PutMapping("/admin/users/{id}/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable Long id,
                                        @Valid @RequestBody UserDto.UpdateRoleRequest request) {
        try {
            return ResponseEntity.ok(userService.updateRole(id, request.getRole()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(UserDto.MessageResponse.builder()
                            .message(e.getMessage()).success(false).build());
        }
    }

    // PUT /api/admin/users/{id}/toggle - Enable/disable user
    @PutMapping("/admin/users/{id}/toggle")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.toggleUserStatus(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/admin/users/{id}
    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(UserDto.MessageResponse.builder()
                    .message("User deleted successfully").success(true).build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/users/me - Current user profile
    @GetMapping("/users/me")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }
}
