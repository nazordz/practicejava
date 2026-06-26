package com.nazor.practicejava.book;

import java.util.List;
import java.util.UUID;

import com.nazor.practicejava.book.dto.CreateBookRequest;
import com.nazor.practicejava.book.dto.UpdateBookRequest;
import com.nazor.practicejava.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<Book> findAllForUser(UUID userId) {
        return bookRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Book findForUser(UUID id, UUID userId) {
        return bookRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    /** Creates a book owned by the authenticated user. */
    public Book create(User owner, CreateBookRequest request) {
        Book book = new Book();
        book.setUser(owner);
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setGenre(request.genre());
        book.setIsbn(request.isbn());
        book.setPublishedYear(request.publishedYear());
        book.setPrice(request.price());
        return bookRepository.save(book);
    }

    /** Updates a book, but only if it belongs to the given user. */
    public Book update(UUID id, UUID userId, UpdateBookRequest request) {
        Book book = findForUser(id, userId);

        if (request.title() != null) {
            book.setTitle(request.title());
        }
        if (request.author() != null) {
            book.setAuthor(request.author());
        }
        if (request.genre() != null) {
            book.setGenre(request.genre());
        }
        if (request.isbn() != null) {
            book.setIsbn(request.isbn());
        }
        if (request.publishedYear() != null) {
            book.setPublishedYear(request.publishedYear());
        }
        if (request.price() != null) {
            book.setPrice(request.price());
        }
        return bookRepository.save(book);
    }

    public void delete(UUID id, UUID userId) {
        Book book = findForUser(id, userId);
        bookRepository.delete(book);
    }
}
