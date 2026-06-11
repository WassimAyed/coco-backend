-- Database creation (to be run in MySQL)
-- CREATE DATABASE IF NOT EXISTS subscription_db;
-- USE subscription_db;

-- 1. Table: subscription_plans
CREATE TABLE IF NOT EXISTS subscription_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL, -- FREE, MONTHLY, YEARLY, PAY_PER_POST
    price DOUBLE NOT NULL,
    post_limit INT NULL,       -- Only for FREE or PAY_PER_POST
    duration_days INT NULL,    -- Only for MONTHLY/YEARLY
    type VARCHAR(50) NOT NULL  -- FREE, SUBSCRIPTION, PAY_PER_POST
);

-- 2. Table: user_subscriptions
CREATE TABLE IF NOT EXISTS user_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) NOT NULL, -- ACTIVE, EXPIRED, CANCELED
    remaining_posts INT NULL,    -- Only for FREE or PAY_PER_POST
    CONSTRAINT fk_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans(id)
);

-- 3. Table: payments
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    subscription_id BIGINT NOT NULL,
    stripe_payment_id VARCHAR(255),
    amount DOUBLE NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL, -- SUCCESS, FAILED, PENDING
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscription FOREIGN KEY (subscription_id) REFERENCES user_subscriptions(id)
);

-- 4. Sample Data for subscription_plans
INSERT INTO subscription_plans (name, price, post_limit, duration_days, type) VALUES
('FREE_PROMO', 0.0, 5, NULL, 'FREE'),
('MONTHLY', 19.99, NULL, 30, 'SUBSCRIPTION'),
('YEARLY', 199.99, NULL, 365, 'SUBSCRIPTION'),
('PAY_PER_POST', 3.0, 1, NULL, 'PAY_PER_POST');
