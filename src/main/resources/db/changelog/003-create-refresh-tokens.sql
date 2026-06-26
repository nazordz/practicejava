--liquibase formatted sql
-- db/changelog/003-create-refresh-tokens.sql

--changeset nazor:003-create-refresh-tokens
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMPTZ NOT NULL,
    -- Revocation marker: a token is only accepted while this is FALSE.
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_refresh_tokens_user_id ON refresh_tokens (user_id);
-- Speeds up "find the user's active tokens" lookups used during revocation.
CREATE INDEX ix_refresh_tokens_user_active ON refresh_tokens (user_id) WHERE revoked = FALSE;
--rollback DROP TABLE refresh_tokens;
