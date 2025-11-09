-- Insert dummy project data for testing
INSERT INTO projects (name, description, status, owner_id, created_at, updated_at) VALUES
('Mobile App Development', 'A comprehensive mobile application development project for iOS and Android platforms', 'ACTIVE', 1, NOW(), NOW()),
('Website Redesign', 'Complete redesign of the company website with modern UI/UX', 'ACTIVE', 1, NOW(), NOW()),
('API Integration', 'Integration of third-party APIs for payment processing and notifications', 'ACTIVE', 1, NOW(), NOW());

-- Insert dummy task data for testing  
INSERT INTO tasks (title, description, status, priority, project_id, assignee_id, creator_id, created_at, updated_at, due_date) VALUES
-- Tasks for Mobile App Development project (id: 1)
('Design UI mockups', 'Create detailed UI mockups for all mobile app screens', 'IN_PROGRESS', 'HIGH', 1, 1, 1, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY)),
('Set up development environment', 'Configure development tools and framework setup', 'COMPLETED', 'MEDIUM', 1, 1, 1, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 2 DAY)),
('Implement user authentication', 'Develop login and registration functionality', 'TODO', 'HIGH', 1, 1, 1, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 10 DAY)),
('Integrate push notifications', 'Set up push notification service for mobile apps', 'TODO', 'MEDIUM', 1, 1, 1, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY)),

-- Tasks for Website Redesign project (id: 2)  
('Create wireframes', 'Design wireframes for all website pages', 'COMPLETED', 'HIGH', 2, 1, 1, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY)),
('Develop responsive layout', 'Implement responsive CSS layout for all devices', 'IN_PROGRESS', 'HIGH', 2, 1, 1, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 8 DAY)),
('Optimize website performance', 'Improve page load times and SEO optimization', 'TODO', 'MEDIUM', 2, 1, 1, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 12 DAY)),

-- Tasks for API Integration project (id: 3)
('Research payment APIs', 'Research and evaluate different payment processing APIs', 'COMPLETED', 'HIGH', 3, 1, 1, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY)),
('Implement payment gateway', 'Integrate selected payment gateway API', 'IN_PROGRESS', 'URGENT', 3, 1, 1, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY)),
('Set up notification system', 'Implement email and SMS notification APIs', 'TODO', 'MEDIUM', 3, 1, 1, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY));