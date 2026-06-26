package com.nazor.practicejava.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    List<User> findAllByDeletedAtIsNull();

    boolean existsByEmailAndDeletedAtIsNull(String email);
}
