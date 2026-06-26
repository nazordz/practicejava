package com.nazor.practicejava.book;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, UUID> {

    List<Book> findByUserId(UUID userId);

    Optional<Book> findByIdAndUserId(UUID id, UUID userId);
}
