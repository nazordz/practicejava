--liquibase formatted sql
-- db/changelog/002-create-books.sql

--changeset nazor:007-create-books
CREATE TABLE books (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    genre VARCHAR(128),
    isbn VARCHAR(32),
    published_year INT,
    price NUMERIC(10, 2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Books are looked up by their owning user most of the time.
CREATE INDEX ix_books_user_id ON books (user_id);

--rollback DROP TABLE books;
