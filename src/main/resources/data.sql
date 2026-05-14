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

INSERT INTO USERS (EMAIL, NAME, PASSWORD) VALUES
(
    'admin@mail.com',
    'Admin User',
    '$2a$10$GmlnkUK0bmBWerLak6nEZOj6qyjbmHcK5.wvtjAhAKYb4E0jFtHhS'
);

INSERT INTO USERS (EMAIL, NAME, PASSWORD) VALUES
(
    'seller1@mail.com',
    'Seller One',
    '$2a$10$GmlnkUK0bmBWerLak6nEZOj6qyjbmHcK5.wvtjAhAKYb4E0jFtHhS'
);

INSERT INTO USERS (EMAIL, NAME, PASSWORD) VALUES
(
    'seller2@mail.com',
    'Seller Two',
    '$2a$10$GmlnkUK0bmBWerLak6nEZOj6qyjbmHcK5.wvtjAhAKYb4E0jFtHhS'
);

INSERT INTO USERS (EMAIL, NAME, PASSWORD) VALUES
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
-- =========================

INSERT INTO PRODUCTS
(
    PRODUCT_ID,
    PRODUCT_NAME,
    DESCRIPTION,
    PRICE,
    QUANTITY,
    CATEGORY_ID
)
VALUES
(
    1,
    'CAT 320 Hydraulic Pump',
    'Heavy duty hydraulic pump for CAT excavators',
    4500,
    15,
    1
);

INSERT INTO PRODUCTS
(
    PRODUCT_ID,
    PRODUCT_NAME,
    DESCRIPTION,
    PRICE,
    QUANTITY,
    CATEGORY_ID
)
VALUES
(
    2,
    'Komatsu Engine Filter',
    'Industrial engine oil filter',
    120,
    50,
    2
);