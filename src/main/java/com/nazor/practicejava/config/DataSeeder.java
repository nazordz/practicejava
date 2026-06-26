package com.nazor.practicejava.config;

import com.nazor.practicejava.user.Role;
import com.nazor.practicejava.user.RoleRepository;
import com.nazor.practicejava.user.User;
import com.nazor.practicejava.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds dev/test data. Roles & permissions are owned by Liquibase
 * (changeset {@code 006-seed-roles-and-permissions}); this seeder only adds
 * users, since their bcrypt password hashes are easier to produce in Java.
 *
 * <p>Activates only under the {@code seed} profile, e.g.
 * {@code ./mvnw spring-boot:run -Dspring-boot.run.profiles=seed}.
 */
@Slf4j
@Component
@Profile("seed")
@Order(1)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("admin@example.com", "Admin User", "password", "ROLE_ADMIN");
        seedUser("user@example.com", "Standard User", "password", "ROLE_USER");
    }

    private void seedUser(String email, String fullName, String rawPassword, String roleName) {
        // Idempotent: skip if the user already exists so restarts don't duplicate.
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            log.info("Seed user {} already exists, skipping", email);
            return;
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException(
                        "Role " + roleName + " not found — run Liquibase migrations first"));

        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.addRole(role);

        userRepository.save(user);
        log.info("Seeded user {} with role {}", email, roleName);
    }
}
