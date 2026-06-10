-- ─────────────────────────────────────────────────────────────
--  V2 – Seed Data: Plans, Tiers, Benefits
-- ─────────────────────────────────────────────────────────────

-- Plans (Monthly / Quarterly / Yearly)
INSERT INTO plans (name, duration, duration_in_days, price) VALUES
    ('Monthly Plan',   'MONTHLY',   30,  99.00),
    ('Quarterly Plan', 'QUARTERLY', 90,  249.00),
    ('Yearly Plan',    'YEARLY',    365, 899.00);

-- Tiers (Silver=1, Gold=2, Platinum=3)
INSERT INTO tiers (name, level, rank_order) VALUES
    ('Silver',   'SILVER',   1),
    ('Gold',     'GOLD',     2),
    ('Platinum', 'PLATINUM', 3);

-- ── Silver Benefits ────────────────────────────────────────────────────────
INSERT INTO benefits (tier_id, benefit_type, config_json, description) VALUES
(1, 'SHIPPING',
 '{"type":"SHIPPING","freeDeliveryThreshold":500.00,"maxDeliveryDiscount":50.00,"eligibleCategories":["Electronics","Clothing"]}',
 'Free delivery on orders above ₹500'),

(1, 'DISCOUNT',
 '{"type":"DISCOUNT","discountPercentage":5.00,"maxDiscountAmount":200.00,"applicableCategories":["Electronics","Clothing","Books"]}',
 '5% extra discount on selected categories');

-- ── Gold Benefits ──────────────────────────────────────────────────────────
INSERT INTO benefits (tier_id, benefit_type, config_json, description) VALUES
(2, 'SHIPPING',
 '{"type":"SHIPPING","freeDeliveryThreshold":0.00,"maxDeliveryDiscount":100.00,"eligibleCategories":["Electronics","Clothing","Grocery","Books"]}',
 'Free delivery on all orders'),

(2, 'DISCOUNT',
 '{"type":"DISCOUNT","discountPercentage":10.00,"maxDiscountAmount":500.00,"applicableCategories":["Electronics","Clothing","Books","Grocery"]}',
 '10% extra discount on selected categories'),

(2, 'EXCLUSIVE_DEALS',
 '{"type":"EXCLUSIVE_DEALS","earlyAccessHours":24,"exclusiveCouponCount":2}',
 '24-hour early access to sales + 2 exclusive coupons per month');

-- ── Platinum Benefits ──────────────────────────────────────────────────────
INSERT INTO benefits (tier_id, benefit_type, config_json, description) VALUES
(3, 'SHIPPING',
 '{"type":"SHIPPING","freeDeliveryThreshold":0.00,"maxDeliveryDiscount":200.00,"eligibleCategories":["ALL"]}',
 'Free express delivery on all orders'),

(3, 'DISCOUNT',
 '{"type":"DISCOUNT","discountPercentage":15.00,"maxDiscountAmount":1000.00,"applicableCategories":["ALL"]}',
 '15% extra discount on all items'),

(3, 'EXCLUSIVE_DEALS',
 '{"type":"EXCLUSIVE_DEALS","earlyAccessHours":48,"exclusiveCouponCount":5}',
 '48-hour early access to sales + 5 exclusive coupons per month'),

(3, 'SUPPORT',
 '{"type":"SUPPORT","priorityLevel":"PREMIUM","responseTimeSlaHours":1,"dedicatedManagerAssigned":true}',
 'Dedicated premium support with 1-hour SLA');
