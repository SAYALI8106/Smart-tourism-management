-- ============================================================================
-- Smart Tourism Management System (STMS) — Assignment 9 Migration
-- Safe, Non-Destructive Credential & RBAC Schema Migration
-- ============================================================================
-- This migration script creates the separate USER_CREDENTIAL table without
-- modifying the normalized 3NF structure of the existing Assignment 7 tables.
-- ============================================================================

USE smart_tourism_db;

-- 1. Create USER_CREDENTIAL table if not already existing
CREATE TABLE IF NOT EXISTS user_credential (
    user_id INT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_user_credential_user
        FOREIGN KEY (user_id) REFERENCES user(user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. Populate initial secure BCrypt credentials for existing users
-- Note: 'admin123' BCrypt hash:   $2a$10$atWVQGGaokK6J3wL8/AhAOFsad6P8jJB83R7gRriDHQMb2JZB4feS
--       'tourist123' BCrypt hash: $2a$10$lHWz0hcX5GSQb0JOY3dBFOEow0lXxrSYabrAdfOQ7rRekLePAxk3y

-- Administrator accounts (user_id: 9002 Priya Patil, 9012 Neha Rao)
INSERT INTO user_credential (user_id, password_hash)
SELECT user_id, '$2a$10$atWVQGGaokK6J3wL8/AhAOFsad6P8jJB83R7gRriDHQMb2JZB4feS'
FROM administrator
WHERE user_id NOT IN (SELECT user_id FROM user_credential);

-- Tourist accounts (user_id: 9001 Aarav Sharma, etc.)
INSERT INTO user_credential (user_id, password_hash)
SELECT user_id, '$2a$10$lHWz0hcX5GSQb0JOY3dBFOEow0lXxrSYabrAdfOQ7rRekLePAxk3y'
FROM tourist
WHERE user_id NOT IN (SELECT user_id FROM user_credential);

-- 3. Verification Query
SELECT 
    uc.user_id,
    u.first_name,
    u.last_name,
    u.email,
    CASE 
        WHEN a.user_id IS NOT NULL THEN 'Administrator'
        WHEN t.user_id IS NOT NULL THEN 'Tourist'
        ELSE 'User'
    END AS assigned_role,
    LEFT(uc.password_hash, 15) AS hash_prefix,
    uc.created_at
FROM user_credential uc
JOIN user u ON uc.user_id = u.user_id
LEFT JOIN administrator a ON u.user_id = a.user_id
LEFT JOIN tourist t ON u.user_id = t.user_id
ORDER BY uc.user_id;
