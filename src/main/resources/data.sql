-- =========================
-- ROLES
-- =========================

INSERT INTO ROLES (ROLE_ID, ROLE_NAME)
VALUES (1, 'ROLE_USER');

INSERT INTO ROLES (ROLE_ID, ROLE_NAME)
VALUES (2, 'ROLE_ADMIN');

INSERT INTO ROLES (ROLE_ID, ROLE_NAME)
VALUES (3, 'ROLE_SELLER');

-- =========================
-- USERS
-- password = 1234
-- =========================

INSERT INTO USERS (EMAIL, NAME, PASSWORD)
VALUES
(
    'admin@mail.com',
    'Admin User',
    '$2a$10$GmlnkUK0bmBWerLak6nEZOj6qyjbmHcK5.wvtjAhAKYb4E0jFtHhS'
);

INSERT INTO USERS (EMAIL, NAME, PASSWORD)
VALUES
(
    'seller1@mail.com',
    'Seller One',
    '$2a$10$GmlnkUK0bmBWerLak6nEZOj6qyjbmHcK5.wvtjAhAKYb4E0jFtHhS'
);

INSERT INTO USERS (EMAIL, NAME, PASSWORD)
VALUES
(
    'seller2@mail.com',
    'Seller Two',
    '$2a$10$GmlnkUK0bmBWerLak6nEZOj6qyjbmHcK5.wvtjAhAKYb4E0jFtHhS'
);

INSERT INTO USERS (EMAIL, NAME, PASSWORD)
VALUES
(
    'user@mail.com',
    'Normal User',
    '$2a$10$GmlnkUK0bmBWerLak6nEZOj6qyjbmHcK5.wvtjAhAKYb4E0jFtHhS'
);

-- =========================
-- USER ROLES
-- =========================

INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (1, 2);

INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (2, 3);

INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (3, 3);

INSERT INTO USER_ROLES (USER_ID, ROLE_ID)
VALUES (4, 1);

-- =========================
-- CATEGORIES
-- =========================

INSERT INTO CATEGORIES
(CATEGORY_ID, CATEGORY_NAME)
VALUES
(
    1,
    'Excavator Parts'
);

INSERT INTO CATEGORIES
(CATEGORY_ID, CATEGORY_NAME)
VALUES
(
    2,
    'Hydraulic Systems'
);

-- =========================
-- PRODUCTS
-- PRODUCT_ID removed so H2
-- auto-generates IDs correctly
-- =========================

INSERT INTO PRODUCTS
(
    PRODUCT_NAME,
    DESCRIPTION,
    IMAGE_URL,
    PRICE,
    QUANTITY,
    CATEGORY_ID
)
VALUES
(
    'CAT 320 Hydraulic Pump',
    'Heavy duty hydraulic pump for CAT excavators',
    'https://images.pexels.com/photos/162553/keys-workshop-mechanic-tools-162553.jpeg',
    4500,
    15,
    1
);

INSERT INTO PRODUCTS
(
    PRODUCT_NAME,
    DESCRIPTION,
    IMAGE_URL,
    PRICE,
    QUANTITY,
    CATEGORY_ID
)
VALUES
(
    'Komatsu Engine Filter',
    'Industrial engine oil filter',
    'https://images.pexels.com/photos/3806288/pexels-photo-3806288.jpeg',
    120,
    50,
    2
);