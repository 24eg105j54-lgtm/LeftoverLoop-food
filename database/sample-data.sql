-- Sample Data for LeftoverLoop Platform
-- Default Password for all sample users is: password123

USE leftoverloop_db;

-- 1. Insert Sample Users (Password is hashed 'password123')
-- BCrypt Hash: $2a$10$78G8p0L4K1/qfB2X/dZ3o.Y3Q4M0fQ/5O8wY9f0A.R5A9.V4W8Q1e (or generated at runtime)
INSERT INTO users (id, full_name, email, password, role, phone, address, organization_name, active) VALUES
(1, 'Admin Controller', 'admin@leftoverloop.org', '$2a$10$DOWMh/6Yn4.5B24Zz4/x7O5jL5mE8/2H3q1Y.7/g5zK3B7Y8W6Q1e', 'ROLE_ADMIN', '+1-555-0100', 'HQ Command Center', 'LeftoverLoop Global', TRUE),
(2, 'Grand Plaza Hotel', 'donor@grandplaza.com', '$2a$10$DOWMh/6Yn4.5B24Zz4/x7O5jL5mE8/2H3q1Y.7/g5zK3B7Y8W6Q1e', 'ROLE_DONOR', '+1-555-0101', '742 Evergreen Terrace', 'Grand Plaza Catering', TRUE),
(3, 'Green Grocery Market', 'grocer@greenmarket.com', '$2a$10$DOWMh/6Yn4.5B24Zz4/x7O5jL5mE8/2H3q1Y.7/g5zK3B7Y8W6Q1e', 'ROLE_DONOR', '+1-555-0102', '120 Market Street', 'Green Grocer Inc.', TRUE),
(4, 'Sarah Community Shelter', 'receiver@shelter.org', '$2a$10$DOWMh/6Yn4.5B24Zz4/x7O5jL5mE8/2H3q1Y.7/g5zK3B7Y8W6Q1e', 'ROLE_RECEIVER', '+1-555-0103', '45 Hope Avenue', 'Hope Haven Shelter', TRUE),
(5, 'Food Rescue NGO', 'contact@foodrescuengo.org', '$2a$10$DOWMh/6Yn4.5B24Zz4/x7O5jL5mE8/2H3q1Y.7/g5zK3B7Y8W6Q1e', 'ROLE_ORGANIZATION', '+1-555-0104', '88 Alliance Blvd', 'Food Rescue Alliance', TRUE);

-- 2. Insert Sample Food Donations
INSERT INTO food_donations (id, title, category, description, initial_quantity, unit, preparation_time, expiry_date, location, pickup_instructions, image_url, donor_id, status) VALUES
(1, 'Fresh Banquet Buffet Trays', 'Prepared Meals', 'Warm gourmet pasta, grilled vegetables, and rice pilaf from evening conference banquet.', 25, 'packets', NOW() - INTERVAL 2 HOUR, NOW() + INTERVAL 10 HOUR, '742 Evergreen Terrace, Banquet Kitchen', 'Pick up from Loading Bay 3 near rear elevator.', 'https://images.unsplash.com/photo-1555244162-803834f70033?w=600', 2, 'AVAILABLE'),
(2, 'Assorted Artisan Bread & Pastries', 'Bakery', 'Freshly baked sourdough, baguettes, croissants, and blueberry muffins.', 40, 'items', NOW() - INTERVAL 4 HOUR, NOW() + INTERVAL 20 HOUR, '120 Market Street Bakery Counter', 'Ask for Manager Mark at front register.', 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600', 3, 'AVAILABLE'),
(3, 'Bulk Organic Produce Crates', 'Fresh Produce', 'Crates of crisp apples, oranges, carrots, and leafy spinach.', 50, 'kg', NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 2 DAY, '88 Alliance Blvd Distribution Hub', 'Forklift assisted loading available at Gate A.', 'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=600', 5, 'AVAILABLE'),
(4, 'Chilled Dairy & Yogurt Packs', 'Dairy', 'Sealed organic Greek yogurts and 2% milk cartons nearing sell-by date.', 30, 'packs', NOW() - INTERVAL 12 HOUR, NOW() + INTERVAL 6 HOUR, '120 Market Street Cold Storage', 'Refrigerated van recommended for pickup.', 'https://images.unsplash.com/photo-1563636619-e9143da7973b?w=600', 3, 'AVAILABLE');

-- 3. Insert Sample Food Requests
INSERT INTO food_requests (id, donation_id, receiver_id, requested_quantity, status, collection_code, notes, requested_at) VALUES
(1, 1, 4, 10, 'ACCEPTED', 'LL-893A7F', 'Urgent need for evening dinner serving at shelter.', NOW() - INTERVAL 1 HOUR),
(2, 2, 4, 15, 'PENDING', 'LL-412B9C', 'Breakfast loaves for shelter residents.', NOW() - INTERVAL 30 MINUTE);
