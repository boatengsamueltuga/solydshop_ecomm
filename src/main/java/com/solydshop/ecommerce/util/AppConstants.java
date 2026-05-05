package com.solydshop.ecommerce.util;

public final class AppConstants {

    private AppConstants() {}

    // ==============================
    // Pagination Defaults
    // ==============================
    public static final String PAGE_NUMBER = "0";   // default page index (first page)
    public static final String PAGE_SIZE   = "10";  // default number of records per page

    // ==============================
    // Sorting Defaults
    // ==============================

    // Common sorting direction
    public static final String SORT_DIR_ASC  = "asc";
    public static final String SORT_DIR_DESC = "desc";

    // Category sorting
    public static final String SORT_CATEGORIES_BY  = "categoryId";
    public static final String SORT_CATEGORIES_DIR = SORT_DIR_ASC;

    // Product sorting
    public static final String SORT_PRODUCTS_BY  = "productId";
    public static final String SORT_PRODUCTS_DIR = SORT_DIR_DESC;

    // Order sorting
    public static final String SORT_ORDERS_BY  = "totalAmount";
    public static final String SORT_ORDERS_DIR = SORT_DIR_DESC;

    // User sorting
    public static final String SORT_USERS_BY  = "userId";
    public static final String SORT_USERS_DIR = SORT_DIR_ASC;
}
