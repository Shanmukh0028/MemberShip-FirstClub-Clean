-- ─────────────────────────────────────────────────────────────
--  V4 – Remove auto_renew column (cancel = immediately CANCELLED)
-- ─────────────────────────────────────────────────────────────
ALTER TABLE subscriptions DROP COLUMN auto_renew;
