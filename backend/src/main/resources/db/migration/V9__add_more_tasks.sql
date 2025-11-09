-- Add 6 more tasks for different users to ensure visibility

-- Insert additional tasks for user ID 2 (MANOHAR PAGADALA)
INSERT INTO tasks (title, description, status, priority, project_id, assignee_id, creator_id, created_at, updated_at, due_date) VALUES
-- Tasks for Mobile App Development project (id: 1) assigned to user 2
('Database Integration', 'Set up and configure database connections for the mobile app', 'TODO', 'HIGH', 1, 2, 2, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY)),
('User Profile Management', 'Implement user profile creation and editing functionality', 'IN_PROGRESS', 'MEDIUM', 1, 2, 2, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 12 DAY)),
('Social Media Integration', 'Add social media login and sharing capabilities', 'TODO', 'LOW', 1, 2, 2, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 20 DAY)),

-- Tasks for Website Redesign project (id: 2) assigned to user 2
('Content Management System', 'Implement a modern CMS for easy content updates', 'IN_PROGRESS', 'HIGH', 2, 2, 2, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 9 DAY)),
('Security Implementation', 'Add SSL certificates and security headers', 'TODO', 'URGENT', 2, 2, 2, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY)),

-- Task for API Integration project (id: 3) assigned to user 2
('Documentation and Testing', 'Create comprehensive API documentation and test cases', 'TODO', 'MEDIUM', 3, 2, 2, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 18 DAY));

-- Also create a few tasks where user 2 is the creator but assigned to user 1 for variety
INSERT INTO tasks (title, description, status, priority, project_id, assignee_id, creator_id, created_at, updated_at, due_date) VALUES
('Code Review Process', 'Establish and implement code review guidelines', 'TODO', 'MEDIUM', 1, 1, 2, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY)),
('Performance Optimization', 'Optimize application performance and load times', 'IN_PROGRESS', 'HIGH', 2, 1, 2, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 10 DAY));