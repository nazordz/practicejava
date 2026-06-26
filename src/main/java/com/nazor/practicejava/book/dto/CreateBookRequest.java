package com.nazor.practicejava.book.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Payload for creating a book. Note there is intentionally no {@code userId}
 * field — ownership is taken from the authenticated principal, never the client.
 */
public record CreateBookRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String author,
        @Size(max = 128) String genre,
        @Size(max = 32) String isbn,
        Integer publishedYear,
        @PositiveOrZero BigDecimal price) {
}
