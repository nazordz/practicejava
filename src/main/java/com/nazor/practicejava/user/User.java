package com.nazor.practicejava.user;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @Generated
    @ColumnDefault("uuidv7()")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number", length = 32)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Soft-delete marker. {@code null} means the user is active. */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    // ----- domain helpers -----

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    // ----- UserDetails contract -----

    /**
     * Exposes both role names (e.g. {@code ROLE_ADMIN}) and their granted
     * permissions (e.g. {@code user:read}) as authorities, so authorization
     * can be expressed with {@code hasRole(..)} or {@code hasAuthority(..)}.
     */
    @Override
    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .flatMap(role -> Stream.concat(
                        Stream.of(role.getName()),
                        role.getPermissions().stream().map(Permission::getName)))
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    @Transient
    public String getPassword() {
        return password;
    }

    @Override
    @Transient
    public String getUsername() {
        return email;
    }

    @Override
    @Transient
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    @Transient
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isEnabled() {
        return enabled && !isDeleted();
    }
}
