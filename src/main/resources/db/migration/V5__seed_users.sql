-- BCrypt hash of 'Admin@123' (strength 12)
-- BCrypt hash of 'User@123'  (strength 12)
INSERT INTO app_users (username, password_hash, role) VALUES
('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCKKN0/1pCi8BVW9bItolBu', 'ROLE_ADMIN'),
('user',  '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_USER');
