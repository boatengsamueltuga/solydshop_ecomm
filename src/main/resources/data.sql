-- =========================
-- ROLES
-- =========================
INSERT INTO ROLES (ROLE_ID, ROLE_NAME) VALUES (1, 'ROLE_USER');
INSERT INTO ROLES (ROLE_ID, ROLE_NAME) VALUES (2, 'ROLE_ADMIN');
INSERT INTO ROLES (ROLE_ID, ROLE_NAME) VALUES (3, 'ROLE_SELLER');

-- =========================
-- USERS (password = 1234 encoded)
-- =========================
INSERT INTO USERS (USER_ID, EMAIL, NAME, PASSWORD) VALUES
(1, 'admin@mail.com', 'Admin User', '$2a$10$GmlnkUK0bmBWerLak6nEZOj6qyjbmHcK5.wvtjAhAKYb4E0jFtHhS'),
(2, 'seller1@mail.com', 'Seller One', '$2a$10$GmlnkUK0bmBWerLak6nEZOj6qyjbmHcK5.wvtjAhAKYb4E0jFtHhS'),
(3, 'seller2@mail.com', 'Seller Two', '$2a$10$GmlnkUK0bmBWerLak6nEZOj6qyjbmHcK5.wvtjAhAKYb4E0jFtHhS'),
(4, 'user@mail.com', 'Normal User', '$2a$10$GmlnkUK0bmBWerLak6nEZOj6qyjbmHcK5.wvtjAhAKYb4E0jFtHhS');

-- =========================
-- USER ROLES
-- =========================
-- ADMIN
INSERT INTO USER_ROLES (USER_ID, ROLE_ID) VALUES (1, 2);

-- SELLERS
INSERT INTO USER_ROLES (USER_ID, ROLE_ID) VALUES (2, 3);
INSERT INTO USER_ROLES (USER_ID, ROLE_ID) VALUES (3, 3);

-- NORMAL USER
INSERT INTO USER_ROLES (USER_ID, ROLE_ID) VALUES (4, 1);