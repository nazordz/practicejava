# Practice Java

A Spring Boot 4 REST API for practicing modern Java backend patterns. Built on Java 25
with virtual threads, Spring Security, JPA/Hibernate over PostgreSQL, Liquibase migrations,
and Redis caching. Compiles to a GraalVM native image.

## Tech Stack

- **Java 25** with virtual threads enabled
- **Spring Boot 4.1** (Web MVC, Data JPA, Security, Validation, Cache)
- **PostgreSQL** (`pgvector/pgvector:pg18`) — schema owned by Liquibase (`ddl-auto: none`)
- **Redis** — caching layer
- **Liquibase** — database migrations
- **Datafaker** — dev/test data seeding
- **Testcontainers** — integration tests
- **GraalVM** — native image build

## API

| Method | Path             | Description       |
|--------|------------------|-------------------|
| GET    | `/api/users/me`  | Current user      |
| GET    | `/api/users`     | List users        |
| GET    | `/api/users/{id}`| Get user          |
| POST   | `/api/users`     | Create user       |
| PUT    | `/api/users/{id}`| Update user       |
| DELETE | `/api/users/{id}`| Delete user       |
| GET    | `/api/books`     | List books        |
| GET    | `/api/books/{id}`| Get book          |
| POST   | `/api/books`     | Create book       |
| PUT    | `/api/books/{id}`| Update book       |
| DELETE | `/api/books/{id}`| Delete book       |

Bruno API collection lives in [bruno/](bruno/).

## Prerequisites

- JDK 25 (GraalVM 25 required for native builds)
- Docker (for PostgreSQL + Redis via Compose)

## Run in dev

PostgreSQL and Redis start automatically via `spring-boot-docker-compose` (it reads
[compose.yaml](compose.yaml)), so just run:

```bash
./mvnw spring-boot:run
```

To start the backing services manually instead:

```bash
docker compose up -d
```

## Migration

```bash
# run migrate
./mvnw liquibase:update

# rollback example
./mvnw liquibase:rollback -Dliquibase.rollbackCount=2

# reset
./mvnw liquibase:dropAll
```

## Seed data

Two seeders run under the Spring `seed` profile. `DataSeeder` (`@Order(1)`) inserts
users first, then `BookSeeder` (`@Order(2)`) bulk-inserts books linked to random users.
Both are idempotent, so re-running is safe.

> Liquibase migrations must run first — `DataSeeder` looks up the `ROLE_ADMIN` /
> `ROLE_USER` roles seeded by changeset `006-seed-roles-and-permissions`.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
```

This seeds:

- **Users** — `admin@example.com` and `user@example.com` (both password `password`)
- **Books** — 1000 rows by default, each linked to a random user

Override the book count and batch size via properties:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=seed \
  -Dspring-boot.run.arguments="--seeder.books.count=5000 --seeder.books.batch-size=1000"
```

> Users are skipped if they already exist; books are skipped entirely if the `books`
> table has any rows. To re-seed books, empty the table first.

## Test

```bash
./mvnw test
```

## Build a native image

This project uses the GraalVM `native-maven-plugin` (wired through Spring Boot's
`native` profile). You need a GraalVM 25 JDK on your `PATH` (with `native-image` installed).

### Local native executable

Compile the application ahead-of-time into a standalone binary:

```bash
./mvnw -Pnative native:compile -DskipTests
```

The binary is written to `target/practicejava`. Run it directly — no JVM required:

```bash
./target/practicejava
```

> Note: the native image still needs PostgreSQL and Redis running. Start them first
> with `docker compose up -d`.

### Native container image (Dockerfile)

[Dockerfile](Dockerfile) builds the native image inside GraalVM Alpine (musl/static)
and ships it on a minimal Alpine runtime — no JDK in the final image:

```bash
# Build the image
docker build -t practicejava:native .

# Run it on the Compose network alongside Postgres + Redis
docker compose up -d
docker run --rm -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/postgres \
  -e SPRING_DATA_REDIS_URL=redis://host.docker.internal:6379 \
  practicejava:native
```

### Native image via Spring Boot buildpacks

Alternatively, let Spring Boot build an OCI image with Cloud Native Buildpacks
(no local GraalVM needed):

```bash
./mvnw -Pnative spring-boot:build-image -DskipTests
docker run --rm -p 8080:8080 practicejava:0.0.1-SNAPSHOT
```

The app listens on port `8080`.
