package com.solydshop.ecommerce.controller;

import com.solydshop.ecommerce.entity.Category;
import com.solydshop.ecommerce.payload.request.CategoryRequest;
import com.solydshop.ecommerce.payload.response.CategoryDTO;
import com.solydshop.ecommerce.payload.response.CategoryResponse;
import com.solydshop.ecommerce.payload.response.ProductDTO;
import com.solydshop.ecommerce.payload.response.ProductResponse;
import com.solydshop.ecommerce.service.CategoryService;
import com.solydshop.ecommerce.util.AppConstants;

import jakarta.validation.Valid;
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
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_CATEGORIES_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_CATEGORIES_DIR) String sortOrder

    ) {

        CategoryResponse response = categoryService.getAllCategories(
                pageNumber, pageSize, sortBy, sortOrder
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request
    ) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/categories/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/admin/categories/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {

        categoryService.deleteCategory(id);

        return ResponseEntity.ok("Category deleted successfully");
    }

}