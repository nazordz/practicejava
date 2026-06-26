package com.nazor.practicejava.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Bulk-seeds the {@code books} table with Datafaker-generated rows, each linked
 * to a randomly chosen existing user. Inserts are sent in batches via
 * {@link JdbcTemplate#batchUpdate} for throughput, and the DB fills {@code id}
 * (uuidv7) and the timestamp columns from their defaults.
 *
 * <p>Activates only under the {@code seed} profile and runs after
 * {@link DataSeeder} (ordered by {@code @Order}) so users exist first. Idempotent:
 * skips entirely if the table already holds rows.
 */
@Slf4j
@Component
@Profile("seed")
@Order(2)
@RequiredArgsConstructor
public class BookSeeder implements CommandLineRunner {

    private static final String INSERT_SQL = """
            INSERT INTO books (user_id, title, author, genre, isbn, published_year, price)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final Faker faker = new Faker();

    @Value("${seeder.books.count:1000}")
    private int totalBooks;

    @Value("${seeder.books.batch-size:500}")
    private int batchSize;

    @Override
    public void run(String... args) {
        Long existing = jdbcTemplate.queryForObject("SELECT count(*) FROM books", Long.class);
        if (existing != null && existing > 0) {
            log.info("books table already has {} rows, skipping seed", existing);
            return;
        }

        List<UUID> userIds = jdbcTemplate.queryForList("SELECT id FROM users", UUID.class);
        if (userIds.isEmpty()) {
            log.warn("No users found — seed users before books. Skipping book seed.");
            return;
        }

        for (int start = 0; start < totalBooks; start += batchSize) {
            int end = Math.min(start + batchSize, totalBooks);
            List<Object[]> batch = new ArrayList<>(end - start);
            for (int i = start; i < end; i++) {
                UUID userId = userIds.get(faker.random().nextInt(userIds.size()));
                batch.add(new Object[] {
                        userId,
                        faker.book().title(),
                        faker.book().author(),
                        faker.book().genre(),
                        faker.code().isbn13(),
                        faker.number().numberBetween(1950, 2026),
                        BigDecimal.valueOf(faker.number().randomDouble(2, 5, 200))
                });
            }
            jdbcTemplate.batchUpdate(INSERT_SQL, batch);
            log.info("Seeded books {}..{}", start, end);
        }
        log.info("Seeded {} books across {} users", totalBooks, userIds.size());
    }
}
