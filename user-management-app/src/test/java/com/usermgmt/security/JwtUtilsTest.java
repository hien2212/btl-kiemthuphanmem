package com.usermgmt.test.security;

import com.usermgmt.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtils - Unit Tests")
class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // Secret phải >= 32 ký tự cho HMAC-SHA256
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret",
                "test-secret-key-at-least-32-characters-long!");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpiration", 3600000L); // 1 giờ

        userDetails = User.withUsername("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("generateToken: tạo token không null và không rỗng")
    void generateToken_shouldReturnNonEmptyToken() {
        String token = jwtUtils.generateToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("extractUsername: trích xuất đúng username từ token")
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtUtils.generateToken(userDetails);
        String extracted = jwtUtils.extractUsername(token);
        assertEquals("testuser", extracted);
    }

    @Test
    @DisplayName("validateToken: token hợp lệ trả về true")
    void validateToken_withValidToken_shouldReturnTrue() {
        String token = jwtUtils.generateToken(userDetails);
        assertTrue(jwtUtils.validateToken(token, userDetails));
    }

    @Test
    @DisplayName("validateToken: token sai username trả về false")
    void validateToken_withWrongUsername_shouldReturnFalse() {
        String token = jwtUtils.generateToken(userDetails);
        UserDetails otherUser = User.withUsername("otheruser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        assertFalse(jwtUtils.validateToken(token, otherUser));
    }

    @Test
    @DisplayName("validateToken: token giả mạo trả về false")
    void validateToken_withFakeToken_shouldReturnFalse() {
        assertFalse(jwtUtils.validateToken("fake.token.here", userDetails));
    }

    @Test
    @DisplayName("validateToken: token hết hạn trả về false")
    void validateToken_withExpiredToken_shouldReturnFalse() {
        ReflectionTestUtils.setField(jwtUtils, "jwtExpiration", -1000L); // hết hạn ngay
        String expiredToken = jwtUtils.generateToken(userDetails);
        assertFalse(jwtUtils.validateToken(expiredToken, userDetails));
    }
}
