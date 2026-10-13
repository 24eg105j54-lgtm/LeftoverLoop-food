-- LeftoverLoop Food Redistribution Platform MySQL Schema
-- Compatible with Local MySQL and Aiven Cloud MySQL

CREATE DATABASE IF NOT EXISTS leftoverloop_db;
USE leftoverloop_db;

-- Drop tables if exists
DROP TABLE IF EXISTS food_requests;
DROP TABLE IF EXISTS food_donations;
DROP TABLE IF EXISTS users;

-- 1. Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    phone VARCHAR(30),
    address VARCHAR(255),
    organization_name VARCHAR(150),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Food Donations Table
CREATE TABLE food_donations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    initial_quantity INT NOT NULL,
    unit VARCHAR(30) NOT NULL,
    preparation_time DATETIME NOT NULL,
    expiry_date DATETIME NOT NULL,
    location VARCHAR(255) NOT NULL,
    pickup_instructions TEXT,
    image_url VARCHAR(500),
    donor_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_donation_donor (donor_id),
    INDEX idx_donation_status (status),
    INDEX idx_donation_expiry (expiry_date),
    INDEX idx_donation_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Food Requests Table
CREATE TABLE food_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    donation_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    requested_quantity INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    collection_code VARCHAR(50),
    notes TEXT,
    requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    collected_at DATETIME,
    FOREIGN KEY (donation_id) REFERENCES food_donations(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_request_donation (donation_id),
    INDEX idx_request_receiver (receiver_id),
    INDEX idx_request_code (collection_code),
    INDEX idx_request_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
