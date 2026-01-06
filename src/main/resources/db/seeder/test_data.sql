-- ================================================================================================
-- UNIVERSAL SEEDER - FIXED DOCUMENT ENUMS & ADDING HTML CONTENT
-- ================================================================================================

-- Existing Users from V2 (Reference)
-- ADMIN: 4af76b85-6971-47b1-a4aa-0f83da58f2f5
-- USER: 417c9992-8bd1-41f2-852e-54b3e7ca7667

-- ================================================================================================
-- 1. USERS & ROLES
-- ================================================================================================
INSERT INTO users (id, avatar_url, email, enabled, full_name, password, phone, provider, student_code, class_code, registration_date)
VALUES 
('11111111-1111-1111-1111-111111111111', 'https://avatar.iran.liara.run/public/1', 'student_a@example.com', true, 'Nguyen Van Student A', '$2a$10$vSOxFQ8Qr2i7421Q8C3rvu0HkY0fQQ/tT0xM2WtJ/2V4/YECL9GRO', '0900000001', 'LOCAL', 'B19DCCN001', 'D19CQCN01', NOW()),
('22222222-2222-2222-2222-222222222222', 'https://avatar.iran.liara.run/public/2', 'student_b@example.com', true, 'Tran Thi Student B', '$2a$10$vSOxFQ8Qr2i7421Q8C3rvu0HkY0fQQ/tT0xM2WtJ/2V4/YECL9GRO', '0900000002', 'LOCAL', 'B19DCCN002', 'D19CQCN01', NOW()),
('33333333-3333-3333-3333-333333333333', 'https://avatar.iran.liara.run/public/3', 'manager_c@example.com', true, 'Le Van Manager C', '$2a$10$vSOxFQ8Qr2i7421Q8C3rvu0HkY0fQQ/tT0xM2WtJ/2V4/YECL9GRO', '0900000003', 'LOCAL', 'B19DCCN003', 'D19CQCN01', NOW()),
('4af76b85-6971-47b1-a4aa-0f83da58f2f5', 'https://avatar.iran.liara.run/public/4', 'admin@gmail.com', true, 'System Admin', '$2b$10$EHINiaamdhCNxI08hN0HGuLhvx8GF3guszSjXgU6kubrlqK5.JyI6', '0900000000', 'LOCAL', 'ADMIN001', 'ADMIN', NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id) VALUES
('11111111-1111-1111-1111-111111111111', 'USER'),
('22222222-2222-2222-2222-222222222222', 'USER'),
('33333333-3333-3333-3333-333333333333', 'USER'),
('4af76b85-6971-47b1-a4aa-0f83da58f2f5', 'ADMIN')
ON CONFLICT DO NOTHING;

-- ================================================================================================
-- 2. ACADEMIC DATA
-- ================================================================================================
INSERT INTO semesters (id, semester_type, school_year) VALUES
(1, 'FIRST', 2023),
(2, 'SECOND', 2023),
(3, 'SUMMER', 2023)
ON CONFLICT (id) DO NOTHING;

INSERT INTO subjects (id, subject_name, subject_code, credit, description, created_date, last_modified_date) VALUES
('a0000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Introduction to Programming', 'INT101', 3, 'Basic C++ programming', NOW(), NOW()),
('a0000002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Data Structures', 'INT102', 4, 'Lists, Trees, Graphs', NOW(), NOW()),
('a0000003-cccc-cccc-cccc-cccccccccccc', 'Web Development', 'INT103', 3, 'HTML, CSS, JS, React', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO subject_references (id, semester_type, subject_id, faculty_id, semester_id, cohort_code) VALUES
('b0000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'FIRST', 'a0000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'e94e3fe5-33d1-4b55-bf1d-771fab5ed233', 1, 'D23'),
('b0000002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'SECOND', 'a0000002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'e94e3fe5-33d1-4b55-bf1d-771fab5ed233', 2, 'D23')
ON CONFLICT (id) DO NOTHING;

-- ================================================================================================
-- 3. GRADES & PROFILES
-- ================================================================================================
INSERT INTO cpa_profiles (id, cpa_profile_name, cpa_profile_code, letter_cpa_score, number_cpa_score, total_accumulated_score, accumulated_credits, user_id, created_at) VALUES
('c0000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'CPA Profile for Student A', 'CPA_STUDENT_A', 'A', 3.6, 3.6, 120, '11111111-1111-1111-1111-111111111111', NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO gpa_profiles (id, gpa_profile_code, letter_gpa_score, number_gpa_score, total_weighted_score, passed_credits, cpa_profile_id, created_at) VALUES
('d0000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'GPA_SEM1_STUD_A', 'A', 3.8, 3.8, 20, 'c0000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO grade_subject_average_profiles (id, letter_current_score, current_score, gpa_profile_id, subject_reference_id, created_at) VALUES
('e0000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'A', 9.5, 'd0000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'b0000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW())
ON CONFLICT (id) DO NOTHING;

-- ================================================================================================
-- 4. FORUM
-- ================================================================================================
INSERT INTO categories (id, name, description, created_at, category_type, creator) VALUES
('f0000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Life & Campus', 'Chuyện đời sống', NOW(), 'LIFE', '4af76b85-6971-47b1-a4aa-0f83da58f2f5'),
('f0000002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Study Help', 'Help each other', NOW(), 'ACADEMIC', '4af76b85-6971-47b1-a4aa-0f83da58f2f5')
ON CONFLICT (id) DO NOTHING;

INSERT INTO topics (id, title, created_at, last_modified_at, content, topic_visibility, category_id, created_by, deleted, lastModifiedAt) VALUES
('90000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Java 101', NOW(), NOW(), 'Discuss Java here', 'PUBLIC', 'f0000002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-1111-1111-1111-111111111111', false, NOW()),
('90000002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Off-topic', NOW(), NOW(), 'Anything goes', 'PUBLIC', 'f0000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', false, NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO topic_members (id, topic_role, approved, joined_at, user_id, topic_id) VALUES
(uuid_generate_v4(), 'MANAGER', true, NOW(), '33333333-3333-3333-3333-333333333333', '90000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
(uuid_generate_v4(), 'MEMBER', true, NOW(), '11111111-1111-1111-1111-111111111111', '90000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa')
ON CONFLICT (id) DO NOTHING;

INSERT INTO posts (id, title, content, created_date_time, last_modified_date_time, post_status, approved_by, approved_at, topic_id, author_id, reaction_count, deleted) VALUES
('80000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'How to use Streams?', 'Can someone explain Java Streams?', NOW(), NOW(), 'APPROVED', '33333333-3333-3333-3333-333333333333', NOW(), '90000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 5, false),
('80000002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Weekend plans?', 'Who is going to the hackathon?', NOW(), NOW(), 'PENDING', NULL, NULL, '90000002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 0, false)
ON CONFLICT (id) DO NOTHING;

INSERT INTO comments (id, post_id, author_id, parent_id, content, created_date_time, reaction_count, deleted, is_accepted) VALUES
('70000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '80000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', NULL, 'Use .stream() on collections!', NOW(), 2, false, true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO reactions (user_id, target_id, target_type, type, created_at) VALUES
('33333333-3333-3333-3333-333333333333', '80000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'POST', 'LIKE', NOW())
ON CONFLICT (user_id, target_id, target_type) DO NOTHING;

-- ================================================================================================
-- 5. DOCUMENTS
-- ================================================================================================
-- FIX: 'PDF' is invalid. Valid Enums: COURSE_BOOK, SLIDE, EXAM
INSERT INTO documents (id, title, description, file_path, document_status, document_type, size, original_filename, created_at, subject_id, uploaded_by, visibility) VALUES
('60000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Java Cheat Sheet', 'Quick ref for Java', '/files/java_cheat_sheet.pdf', 'APPROVED', 'COURSE_BOOK', 1024, 'java_cheat_sheet.pdf', NOW(), 'a0000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333', 'PUBLIC')
ON CONFLICT (id) DO NOTHING;

INSERT INTO file_metadata (id, file_name, folder, url, content_type, size, resource_type, resource_id, created_at, user_id) VALUES
('50000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'java_cheat_sheet.pdf', 'docs', 'https://example.com/files/java_cheat_sheet.pdf', 'application/pdf', 1024, 'DOCUMENT', '60000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW(), '33333333-3333-3333-3333-333333333333')
ON CONFLICT (id) DO NOTHING;

-- ================================================================================================
-- 6. ANNOUNCEMENTS & NOTIFICATIONS
-- ================================================================================================
INSERT INTO announcements (id, announcement_status, announcement_type, content, title, created_by, created_date, announcement_provider) VALUES
('40000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', true, 'ACADEMIC', 'Final exams serve everyone!', 'Exam Schedule', '4af76b85-6971-47b1-a4aa-0f83da58f2f5', NOW(), 'PORTAL_PTIT'),
('40000002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', true, 'ACADEMIC', 'Scholarship opportunities for 2024', 'Scholarship News', '4af76b85-6971-47b1-a4aa-0f83da58f2f5', NOW(), 'PORTAL_PTIT'),
('40000003-cccc-cccc-cccc-cccccccccccc', true, 'ACADEMIC', 'Library opening hours changed', 'Library Notice', '4af76b85-6971-47b1-a4aa-0f83da58f2f5', NOW(), 'PORTAL_PTIT'),
('40000004-dddd-dddd-dddd-dddddddddddd', true, 'ACADEMIC', 'New course registration system', 'Registration Update', '4af76b85-6971-47b1-a4aa-0f83da58f2f5', NOW(), 'DAO_TAO_PTIT'),
('40000005-eeee-eeee-eeee-eeeeeeeeeeee', true, 'ACADEMIC', '<p>Dear Students,</p><p>We are pleased to announce the <strong>Graduation Ceremony 2024</strong> details.</p><ul><li>Date: 20th Jan</li><li>Location: Great Hall</li></ul><p><em>Please arrive on time.</em></p>', 'Graduation 2024 (HTML Test)', '4af76b85-6971-47b1-a4aa-0f83da58f2f5', NOW(), 'GIAO_VU_PTIT')
ON CONFLICT (id) DO NOTHING;

INSERT INTO announcement_targets (id, classroom_code, announcement_id) VALUES
(uuid_generate_v4(), 'D19CQCN01', '40000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
(uuid_generate_v4(), 'D19CQCN01', '40000002-bbbb-bbbb-bbbb-bbbbbbbbbbbb'),
(uuid_generate_v4(), 'D19CQCN01', '40000003-cccc-cccc-cccc-cccccccccccc'),
(uuid_generate_v4(), 'D19CQCN01', '40000004-dddd-dddd-dddd-dddddddddddd'),
(uuid_generate_v4(), 'D19CQCN01', '40000005-eeee-eeee-eeee-eeeeeeeeeeee')
ON CONFLICT (id) DO NOTHING;

INSERT INTO notification_events (id, content, related_id, title, type, created_by, created_at) VALUES
('30000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'New exam schedule released', '40000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Exam Alert', 'ANNOUNCEMENT', '4af76b85-6971-47b1-a4aa-0f83da58f2f5', NOW()),
('30000002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'New scholarship news', '40000002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Scholarship Alert', 'ANNOUNCEMENT', '4af76b85-6971-47b1-a4aa-0f83da58f2f5', NOW()),
('30000003-cccc-cccc-cccc-cccccccccccc', 'Library hours update', '40000003-cccc-cccc-cccc-cccccccccccc', 'Library Alert', 'ANNOUNCEMENT', '4af76b85-6971-47b1-a4aa-0f83da58f2f5', NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_notifications (id, delivered_at, notification_status, notification_event_id, user_id) VALUES
(uuid_generate_v4(), NOW(), 'SENT', '30000001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111'),
(uuid_generate_v4(), NOW(), 'SENT', '30000002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-1111-1111-1111-111111111111'),
(uuid_generate_v4(), NOW(), 'SENT', '30000003-cccc-cccc-cccc-cccccccccccc', '11111111-1111-1111-1111-111111111111')
ON CONFLICT (id) DO NOTHING;

-- ================================================================================================
-- 7. LOGS & REPORTS
-- ================================================================================================
INSERT INTO activity_logs (id, user_id, action, module, description, created_at) VALUES
(uuid_generate_v4(), '11111111-1111-1111-1111-111111111111', 'LOGIN', 'USER', 'User logged in', NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO reports (id, reporter_id, reason, description, status, target_type, post_id, created_at) VALUES
(uuid_generate_v4(), '11111111-1111-1111-1111-111111111111', 'SPAM', 'This is spam', 'PENDING', 'POST', '80000002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', NOW())
ON CONFLICT (id) DO NOTHING;
