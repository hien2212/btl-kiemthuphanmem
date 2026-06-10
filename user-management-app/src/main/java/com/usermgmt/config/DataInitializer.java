package com.usermgmt.config;

import com.usermgmt.entity.User;
import com.usermgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create default admin
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ROLE_ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("✅ Default admin created: admin / admin123");
        }

        // Create default user
        if (!userRepository.existsByUsername("user1")) {
            User user = User.builder()
                    .username("user1")
                    .email("user1@example.com")
                    .password(passwordEncoder.encode("user123"))
                    .role(User.Role.ROLE_USER)
                    .enabled(true)
                    .build();
            userRepository.save(user);
            log.info("✅ Default user created: user1 / user123");
        }

        log.info("🚀 App running at http://localhost:8080");
        log.info("🗄️  H2 Console at http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:userdb)");
    }
}
