package com.usermgmt.test.entity;

import com.usermgmt.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity - Unit Tests")
class UserTest {

    @Test
    @DisplayName("Builder: tạo User với đầy đủ thông tin")
    void builder_shouldCreateUserCorrectly() {
        User user = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .password("encoded")
                .role(User.Role.ROLE_USER)
                .enabled(true)
                .build();

        assertEquals(1L, user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals(User.Role.ROLE_USER, user.getRole());
        assertTrue(user.isEnabled());
    }

    @Test
    @DisplayName("setEnabled: thay đổi trạng thái kích hoạt")
    void setEnabled_shouldUpdateCorrectly() {
        User user = User.builder().enabled(true).build();
        user.setEnabled(false);
        assertFalse(user.isEnabled());
    }

    @Test
    @DisplayName("Role enum: ROLE_USER và ROLE_ADMIN tồn tại")
    void roleEnum_shouldHaveCorrectValues() {
        assertEquals("ROLE_USER",  User.Role.ROLE_USER.name());
        assertEquals("ROLE_ADMIN", User.Role.ROLE_ADMIN.name());
    }

    @Test
    @DisplayName("setRole: thay đổi role")
    void setRole_shouldUpdateCorrectly() {
        User user = User.builder().role(User.Role.ROLE_USER).build();
        user.setRole(User.Role.ROLE_ADMIN);
        assertEquals(User.Role.ROLE_ADMIN, user.getRole());
    }

    @Test
    @DisplayName("NoArgsConstructor: tạo User rỗng không lỗi")
    void noArgsConstructor_shouldWork() {
        User user = new User();
        assertNotNull(user);
    }
}
