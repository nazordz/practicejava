package com.nazor.practicejava.user;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.nazor.practicejava.user.dto.CreateUserRequest;
import com.nazor.practicejava.user.dto.UpdateUserRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAllByDeletedAtIsNull();
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User create(CreateUserRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new DataIntegrityViolationException("Email already in use: " + request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhoneNumber(request.phoneNumber());
        user.setRoles(resolveRoles(request.roles()));
        return userRepository.save(user);
    }

    public User update(UUID id, UpdateUserRequest request) {
        User user = findById(id);

        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber());
        }
        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }
        if (request.roles() != null) {
            user.setRoles(resolveRoles(request.roles()));
        }
        return userRepository.save(user);
    }

    /** Soft delete: mark the user as deleted rather than removing the row. */
    public void delete(UUID id) {
        User user = findById(id);
        user.setDeletedAt(Instant.now());
        userRepository.save(user);
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new IllegalStateException("Default role ROLE_USER is not seeded"));
            return Set.of(defaultRole);
        }
        return roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + name)))
                .collect(Collectors.toSet());
    }
}
