package com.nazor.practicejava.book;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.nazor.practicejava.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
public class Book {

    @Id
    @Generated
    @ColumnDefault("uuidv7()")
    @Column(updatable = false, nullable = false)
    private UUID id;

    /** Owning user. {@code LAZY} to avoid loading the user on every book read. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(length = 128)
    private String genre;

    @Column(length = 32)
    private String isbn;

    @Column(name = "published_year")
    private Integer publishedYear;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
