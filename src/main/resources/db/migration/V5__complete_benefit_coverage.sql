-- ─────────────────────────────────────────────────────────────────────────────
--  V5 – Complete Benefit Coverage
--
--  Ensures every tier has a row for every BenefitType so that no tier returns
--  an empty list regardless of which type filter the caller uses.
--
--  Missing combinations before this migration:
--    Silver  → EXCLUSIVE_DEALS, SUPPORT
--    Gold    → SUPPORT
-- ─────────────────────────────────────────────────────────────────────────────

-- ── Silver: EXCLUSIVE_DEALS ───────────────────────────────────────────────────
--  Silver members have no early-access window and no exclusive coupons.
--  Returning explicit zeros lets consuming services display "not eligible"
--  rather than treating a missing row as an error.
INSERT INTO benefits (tier_id, benefit_type, config_json, description) VALUES
(1, 'EXCLUSIVE_DEALS',
 '{"type":"EXCLUSIVE_DEALS","earlyAccessHours":0,"exclusiveCouponCount":0}',
 'No early access or exclusive coupons – upgrade to Gold or above');

-- ── Silver: SUPPORT ───────────────────────────────────────────────────────────
INSERT INTO benefits (tier_id, benefit_type, config_json, description) VALUES
(1, 'SUPPORT',
 '{"type":"SUPPORT","priorityLevel":"STANDARD","responseTimeSlaHours":72,"dedicatedManagerAssigned":false}',
 'Standard email support with 72-hour response SLA');

-- ── Gold: SUPPORT ─────────────────────────────────────────────────────────────
INSERT INTO benefits (tier_id, benefit_type, config_json, description) VALUES
(2, 'SUPPORT',
 '{"type":"SUPPORT","priorityLevel":"PRIORITY","responseTimeSlaHours":4,"dedicatedManagerAssigned":false}',
 'Priority support with 4-hour response SLA');
