package com.nazor.practicejava.book.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Partial update — only non-null fields are applied. */
public record UpdateBookRequest(
        @Size(max = 255) String title,
        @Size(max = 255) String author,
        @Size(max = 128) String genre,
        @Size(max = 32) String isbn,
        Integer publishedYear,
        @PositiveOrZero BigDecimal price) {
}
