package com.nazor.practicejava.book;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.nazor.practicejava.book.dto.BookResponse;
import com.nazor.practicejava.book.dto.CreateBookRequest;
import com.nazor.practicejava.book.dto.UpdateBookRequest;
import com.nazor.practicejava.user.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRUD for the authenticated user's own books. Every endpoint is scoped to the
 * principal: the {@code user_id} column is filled from {@link AuthenticationPrincipal},
 * never from client input, and reads/updates are restricted to the caller's rows.
 *
 * <p>Authentication is enforced by the security filter chain (all non-permitted
 * requests require auth), so {@code user} is guaranteed non-null here.
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookResponse> list(@AuthenticationPrincipal User user) {
        return bookService.findAllForUser(user.getId()).stream()
                .map(BookResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public BookResponse get(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return BookResponse.from(bookService.findForUser(id, user.getId()));
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@AuthenticationPrincipal User user,
                                               @Valid @RequestBody CreateBookRequest request) {
        Book created = bookService.create(user, request);
        return ResponseEntity
                .created(URI.create("/api/books/" + created.getId()))
                .body(BookResponse.from(created));
    }

    @PutMapping("/{id}")
    public BookResponse update(@AuthenticationPrincipal User user,
                               @PathVariable UUID id,
                               @Valid @RequestBody UpdateBookRequest request) {
        return BookResponse.from(bookService.update(id, user.getId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        bookService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
