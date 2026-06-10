-- ─────────────────────────────────────────────────────────────
--  V1 – Initial Schema
-- ─────────────────────────────────────────────────────────────

CREATE TABLE plans (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    duration         VARCHAR(20)  NOT NULL,
    duration_in_days INT          NOT NULL,
    price            DECIMAL(10, 2) NOT NULL
);

CREATE TABLE tiers (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    level      VARCHAR(20) NOT NULL,
    rank_order INT         NOT NULL
);

CREATE TABLE benefits (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    tier_id      BIGINT       NOT NULL,
    benefit_type VARCHAR(50)  NOT NULL,
    config_json  TEXT         NOT NULL,
    description  VARCHAR(500),
    CONSTRAINT fk_benefit_tier FOREIGN KEY (tier_id) REFERENCES tiers(id)
);

CREATE TABLE subscriptions (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    VARCHAR(100)   NOT NULL,
    plan_id    BIGINT         NOT NULL,
    tier_id    BIGINT         NOT NULL,
    start_date TIMESTAMP      NOT NULL,
    end_date   TIMESTAMP      NOT NULL,
    status     VARCHAR(20)    NOT NULL,
    version    BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT fk_sub_plan FOREIGN KEY (plan_id) REFERENCES plans(id),
    CONSTRAINT fk_sub_tier FOREIGN KEY (tier_id) REFERENCES tiers(id)
);

CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_status  ON subscriptions(status);
CREATE INDEX idx_benefits_tier_type    ON benefits(tier_id, benefit_type);
