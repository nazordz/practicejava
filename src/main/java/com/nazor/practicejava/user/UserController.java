package com.nazor.practicejava.user;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.nazor.practicejava.user.dto.CreateUserRequest;
import com.nazor.practicejava.user.dto.UpdateUserRequest;
import com.nazor.practicejava.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** The currently authenticated user's own profile. */
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User user) {
        return UserResponse.from(user);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public List<UserResponse> list() {
        return userService.findAll().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public UserResponse get(@PathVariable UUID id) {
        return UserResponse.from(userService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        User created = userService.create(request);
        return ResponseEntity
                .created(URI.create("/api/users/" + created.getId()))
                .body(UserResponse.from(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:write')")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return UserResponse.from(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
