package com.nazor.practicejava.book.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.nazor.practicejava.book.Book;

public record BookResponse(
        UUID id,
        UUID userId,
        String title,
        String author,
        String genre,
        String isbn,
        Integer publishedYear,
        BigDecimal price,
        Instant createdAt,
        Instant updatedAt) {

    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getUser().getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.getIsbn(),
                book.getPublishedYear(),
                book.getPrice(),
                book.getCreatedAt(),
                book.getUpdatedAt());
    }
}
