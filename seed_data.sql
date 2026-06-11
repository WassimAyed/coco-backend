-- ====================================================
-- COCO PLATFORM REALISTIC SEED DATASET (ADDITIVE VERSION)
-- Optimized for: Spring Boot JPA / Hibernate defaults
-- Strategy: Use High ID Offsets (1000+) to avoid clashing with existing data
-- ====================================================

-- ####################################################
-- 1. USERS & PROFILES (CoCoUserDB)
-- ####################################################

CREATE DATABASE IF NOT EXISTS CoCoUserDB;
USE CoCoUserDB;

SET FOREIGN_KEY_CHECKS = 0;

-- Password: 'password' (BCrypt)
-- Hash: $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xdqD1RPHu67S1K5W

-- INSERT 80 USERS (ID range: 1001 - 1080)
INSERT IGNORE INTO users (id, username, lastname, email, phone, password, role, enabled, locked, two_factor_enabled, image_url) VALUES
(1001, 'Karim', 'Mokaddem', 'seed.karim@esprit.tn', '+216 22 111 222', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xdqD1RPHu67S1K5W', 'ADMIN', 1, 0, 0, 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400'),
(1002, 'Aziz', 'Baaroun', 'seed.aziz@esprit.tn', '+216 23 444 555', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xdqD1RPHu67S1K5W', 'ADMIN', 1, 0, 0, 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400');

-- Generate Users 1003-1080
INSERT IGNORE INTO users (id, username, lastname, email, phone, password, role, enabled, locked, two_factor_enabled, image_url)
SELECT 
    n, 
    ELT(1 + (n % 10), 'Ahmed', 'Mariem', 'Youssef', 'Ons', 'Amine', 'Fatma', 'Khalil', 'Sarra', 'Lina', 'Aziz'),
    'Seed-User', 
    CONCAT('seed', n, '@esprit.tn'), 
    CONCAT('+216 2', LPAD(n, 7, '0')), 
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xdqD1RPHu67S1K5W', 
    'USER', 1, 0, 0, 
    CONCAT('https://i.pravatar.cc/150?u=', n)
FROM (
    WITH RECURSIVE seq AS (SELECT 1003 AS n UNION ALL SELECT n + 1 FROM seq WHERE n < 1080)
    SELECT n FROM seq
) AS numbers;

-- INSERT USER PROFILES (Linked to 1001-1080)
INSERT IGNORE INTO user_profiles (id, age, gender, budget, city, smoker, pets, cleanliness, sleep_schedule, study_level, social_level, accepts_guests, noise_tolerance, latitude, longitude, user_id)
SELECT 
    u.id, 
    FLOOR(19 + RAND() * 7), 
    CASE WHEN u.id % 2 = 0 THEN 'FEMALE' ELSE 'MALE' END,
    250 + (RAND() * 750),
    ELT(1 + FLOOR(RAND() * 7), 'Tunis', 'Ariana', 'Sousse', 'Sfax', 'Manouba', 'Nabeul', 'Monastir'),
    RAND() > 0.8, 
    RAND() > 0.9,
    1 + FLOOR(RAND() * 5),
    ELT(1 + FLOOR(RAND() * 3), 'EARLY_BIRD', 'NIGHT_OWL', 'FLEXIBLE'),
    ELT(1 + FLOOR(RAND() * 5), 'License 1', 'License 3', 'Master 1', 'Ingenieur 2', 'Ingenieur 5'),
    3 + FLOOR(RAND() * 3),
    RAND() > 0.5,
    1 + FLOOR(RAND() * 5),
    36.8 + (RAND() * 0.1),
    10.1 + (RAND() * 0.1),
    u.id
FROM users u 
WHERE u.id >= 1001;

SET FOREIGN_KEY_CHECKS = 1;

-- ####################################################
-- 2. COLLOCATION OFFERS (CoCoCollocationDB)
-- ####################################################

CREATE DATABASE IF NOT EXISTS CoCoCollocationDB;
USE CoCoCollocationDB;

SET FOREIGN_KEY_CHECKS = 0;

-- INSERT 120 OFFERS (ID range: 2001 - 2120)
-- owner_id points to 1063-1080 (Seed Hosts)
INSERT IGNORE INTO colloc_offre (id, titre, description, prix_loc, ville, chambres, meublee, latitude, longitude, created_at, expiry_date, owner_id, notified)
SELECT 
    n, 
    CONCAT(ELT(1 + (n % 5), 'Seed Room near ESPRIT', 'Seed Coloc Ariana', 'Seed Student Housing', 'Seed Studio Sousse', 'Seed Apartment Tunis'), ' #', n),
    'Realistic seed data offer. Fully furnished, student friendly.',
    300 + (RAND() * 800),
    ELT(1 + FLOOR(RAND() * 7), 'Tunis', 'Ariana', 'Sousse', 'Sfax', 'Manouba', 'Nabeul', 'Monastir'),
    1 + FLOOR(RAND() * 3),
    RAND() > 0.2,
    36.7 + (RAND() * 0.2),
    10.1 + (RAND() * 0.2),
    DATE_SUB(CURRENT_DATE, INTERVAL (RAND() * 20) DAY),
    DATE_ADD(CURRENT_DATE, INTERVAL 60 DAY),
    1063 + (n % 17), 
    0
FROM (
    WITH RECURSIVE seq AS (SELECT 2001 AS n UNION ALL SELECT n + 1 FROM seq WHERE n < 2120)
    SELECT n FROM seq
) AS numbers;

-- ####################################################
-- 3. OFFER IMAGES (Auto-increment IDs)
-- ####################################################

INSERT IGNORE INTO colloc_offre_image (filename, url, offre_id)
SELECT 
    CONCAT('seed_img_', o.id, '_', im.n, '.jpg'),
    CONCAT('https://images.unsplash.com/photo-', ELT(im.n, '1522771739844-6a9f6d5f14af', '1598928506311-c55ded91a20c', '1554995207-c18c203602cb', '1493809842364-78817add7ffb'), '?w=800'),
    o.id
FROM colloc_offre o, (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4) im
WHERE o.id >= 2001;

-- ####################################################
-- 4. OFFERS REQUESTS (Auto-increment IDs)
-- ####################################################

INSERT IGNORE INTO colloc_offre_request (offer_id, student_id, message, status, created_at)
SELECT 
    2001 + FLOOR(RAND() * 119),
    1003 + (n % 60), -- Seed Users 1003-1062
    ELT(1 + (n % 4), 'Bonjour, je suis intéressé par cette offre seed.', 'Is this room still available?', 'Interested student from ESPRIT.', 'Available for visit next week?'),
    ELT(1 + (n % 3), 'ENCOURS', 'ACCEPTEE', 'REJETEE'),
    DATE_SUB(NOW(), INTERVAL (RAND() * 5) DAY)
FROM (
    WITH RECURSIVE seq AS (SELECT 1 AS n UNION ALL SELECT n + 1 FROM seq WHERE n < 221)
    SELECT n FROM seq
) AS numbers;

-- ####################################################
-- 5. FAVORITES (Auto-increment IDs)
-- ####################################################

INSERT IGNORE INTO colloc_offre_favorite (user_id, offre_id)
SELECT DISTINCT
    1003 + FLOOR(RAND() * 60),
    2001 + FLOOR(RAND() * 119)
FROM (
    WITH RECURSIVE seq AS (SELECT 1 AS n UNION ALL SELECT n + 1 FROM seq WHERE n < 350)
    SELECT n FROM seq
) AS numbers
LIMIT 300;

SET FOREIGN_KEY_CHECKS = 1;
