package com.usermgmt.test.service;

import com.usermgmt.dto.UserDto;
import com.usermgmt.entity.User;
import com.usermgmt.repository.UserRepository;
import com.usermgmt.security.JwtUtils;
import com.usermgmt.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Unit Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .role(User.Role.ROLE_USER)
                .enabled(true)
                .build();
    }

    // ===== register =====

    @Test
    @DisplayName("register: đăng ký thành công với thông tin hợp lệ")
    void register_withValidRequest_shouldReturnUserResponse() {
        UserDto.RegisterRequest req = new UserDto.RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("new@example.com");
        req.setPassword("password123");
        req.setConfirmPassword("password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(
                User.builder().id(2L).username("newuser").email("new@example.com")
                        .password("encoded").role(User.Role.ROLE_USER).enabled(true).build()
        );

        UserDto.UserResponse res = userService.register(req);

        assertEquals("newuser", res.getUsername());
        assertEquals("new@example.com", res.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register: username đã tồn tại → ném IllegalArgumentException")
    void register_withDuplicateUsername_shouldThrow() {
        UserDto.RegisterRequest req = new UserDto.RegisterRequest();
        req.setUsername("testuser");
        req.setEmail("other@example.com");
        req.setPassword("pass123");
        req.setConfirmPassword("pass123");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register: email đã tồn tại → ném IllegalArgumentException")
    void register_withDuplicateEmail_shouldThrow() {
        UserDto.RegisterRequest req = new UserDto.RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("test@example.com");
        req.setPassword("pass123");
        req.setConfirmPassword("pass123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.register(req));
    }

    @Test
    @DisplayName("register: mật khẩu không khớp → ném IllegalArgumentException")
    void register_withPasswordMismatch_shouldThrow() {
        UserDto.RegisterRequest req = new UserDto.RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("new@example.com");
        req.setPassword("pass123");
        req.setConfirmPassword("different");

        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.register(req));
    }

    // ===== login =====

    @Test
    @DisplayName("login: đăng nhập thành công trả về token")
    void login_withValidCredentials_shouldReturnAuthResponse() {
        UserDto.LoginRequest req = new UserDto.LoginRequest();
        req.setUsername("testuser");
        req.setPassword("password123");

        Authentication auth = mock(Authentication.class);
        UserDetails ud = org.springframework.security.core.userdetails.User
                .withUsername("testuser").password("encoded")
                .authorities("ROLE_USER").build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(ud);
        when(jwtUtils.generateToken(ud)).thenReturn("mocked.jwt.token");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

        UserDto.AuthResponse res = userService.login(req);

        assertEquals("mocked.jwt.token", res.getToken());
        assertEquals("testuser", res.getUsername());
        assertEquals("Bearer", res.getType());
    }

    // ===== getAllUsers =====

    @Test
    @DisplayName("getAllUsers: trả về danh sách tất cả user")
    void getAllUsers_shouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));

        List<UserDto.UserResponse> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    // ===== getUserById =====

    @Test
    @DisplayName("getUserById: tìm thấy → trả về UserResponse")
    void getUserById_whenExists_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserDto.UserResponse res = userService.getUserById(1L);
        assertEquals("testuser", res.getUsername());
    }

    @Test
    @DisplayName("getUserById: không tìm thấy → ném IllegalArgumentException")
    void getUserById_whenNotFound_shouldThrow() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(99L));
    }

    // ===== updateRole =====

    @Test
    @DisplayName("updateRole: đổi role thành ROLE_ADMIN thành công")
    void updateRole_withValidRole_shouldUpdate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenReturn(sampleUser);

        UserDto.UserResponse res = userService.updateRole(1L, "ROLE_ADMIN");
        assertNotNull(res);
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("updateRole: role không hợp lệ → ném IllegalArgumentException")
    void updateRole_withInvalidRole_shouldThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        assertThrows(IllegalArgumentException.class,
                () -> userService.updateRole(1L, "ROLE_INVALID"));
    }

    // ===== toggleUserStatus =====

    @Test
    @DisplayName("toggleUserStatus: đảo trạng thái enabled")
    void toggleUserStatus_shouldFlipEnabled() {
        assertTrue(sampleUser.isEnabled());
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userService.toggleUserStatus(1L);

        assertFalse(sampleUser.isEnabled());
        verify(userRepository).save(sampleUser);
    }

    // ===== deleteUser =====

    @Test
    @DisplayName("deleteUser: xoá user tồn tại thành công")
    void deleteUser_whenExists_shouldDelete() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUser: user không tồn tại → ném IllegalArgumentException")
    void deleteUser_whenNotFound_shouldThrow() {
        when(userRepository.existsById(99L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(99L));
    }

    // ===== getProfile =====

    @Test
    @DisplayName("getProfile: tìm đúng user theo username")
    void getProfile_whenExists_shouldReturnUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

        UserDto.UserResponse res = userService.getProfile("testuser");
        assertEquals("testuser", res.getUsername());
        assertEquals("test@example.com", res.getEmail());
    }
}
