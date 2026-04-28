package com.solydshop.ecommerce.controller;

import com.solydshop.ecommerce.payload.response.CategoryResponse;
import com.solydshop.ecommerce.service.CategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/public/categories")
    public ResponseEntity<CategoryResponse> getAllCategories(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @RequestParam(defaultValue = "categoryId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder
    ) {

        CategoryResponse response = categoryService.getAllCategories(
                pageNumber, pageSize, sortBy, sortOrder
        );

        return ResponseEntity.ok(response);
    }
}