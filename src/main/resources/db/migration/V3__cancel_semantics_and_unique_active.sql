-- ─────────────────────────────────────────────────────────────
--  V3 – Cancel semantics + TOCTOU-safe insert guard
-- ─────────────────────────────────────────────────────────────

-- Cancel semantics: cancel no longer changes status; it only disables auto-renewal.
-- The subscription stays ACTIVE (user keeps benefits) until endDate.
ALTER TABLE subscriptions ADD COLUMN auto_renew BOOLEAN NOT NULL DEFAULT TRUE;

-- TOCTOU-safe uniqueness for concurrent subscribe requests.
-- active_token is set to user_id when the subscription is ACTIVE and
-- cleared to NULL when it expires. Because SQL NULL != NULL, multiple
-- expired/cancelled rows for the same user are allowed, but only one
-- ACTIVE row per user can exist at the DB level.
ALTER TABLE subscriptions ADD COLUMN active_token VARCHAR(100);

-- Populate active_token for any rows that may already exist (e.g. tests).
UPDATE subscriptions SET active_token = user_id WHERE status = 'ACTIVE';

-- Unique index that enforces one active subscription per user.
-- H2 (like all standard SQL engines) allows multiple NULLs in a unique index.
CREATE UNIQUE INDEX idx_unique_active_sub ON subscriptions(active_token);
