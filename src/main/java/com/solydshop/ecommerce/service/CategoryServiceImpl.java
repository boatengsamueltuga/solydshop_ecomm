package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.entity.Category;
import com.solydshop.ecommerce.exception.ResourceNotFoundException;
import com.solydshop.ecommerce.payload.request.CategoryRequest;
import com.solydshop.ecommerce.payload.response.CategoryDTO;
import com.solydshop.ecommerce.payload.response.CategoryResponse;
import com.solydshop.ecommerce.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    private CategoryDTO mapToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setCategoryName(category.getCategoryName());
        return dto;
    }

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sort = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Category> pageCategories = categoryRepository.findAll(pageable);

        List<Category> categories = pageCategories.getContent();

        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(this::mapToDTO)
                .toList();

        CategoryResponse response = new CategoryResponse();
        response.setContent(categoryDTOs);
        response.setPageNumber(pageCategories.getNumber());
        response.setPageSize(pageCategories.getSize());
        response.setTotalElements(pageCategories.getTotalElements());
        response.setTotalPages(pageCategories.getTotalPages());
        response.setLastPage(pageCategories.isLast());

        return response;
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {

        categoryRepository.findByCategoryName(request.getCategoryName())
                .ifPresent(c -> {
                    throw new RuntimeException("Category already exists");
                });

        Category category = new Category();
        category.setCategoryName(request.getCategoryName());

        categoryRepository.save(category);

        return getAllCategories(0, 5, "categoryId", "asc");
    }

    @Override
    public CategoryDTO getCategoryById(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id: " + categoryId)
                );

        return mapToDTO(category);
    }

    @Override
    public void deleteCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id: " + categoryId)
                );

        categoryRepository.delete(category);
    }
}