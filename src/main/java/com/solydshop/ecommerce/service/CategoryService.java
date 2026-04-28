package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.response.CategoryResponse;

public interface CategoryService {

    CategoryResponse getAllCategories(
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder
    );
}