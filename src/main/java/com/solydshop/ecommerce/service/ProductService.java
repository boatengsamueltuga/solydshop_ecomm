package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.request.ProductRequest;
import com.solydshop.ecommerce.payload.response.ProductDTO;
import com.solydshop.ecommerce.payload.response.ProductResponse;

public interface ProductService {

    ProductDTO createProduct(ProductRequest request);

    // Get all products with optional filters
    ProductResponse getAllProducts(
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder,
            String keyword,
            Long categoryId,
            Double maxPrice,
            Boolean inStock
    );

    ProductDTO getProductById(Long productId);

    void deleteProduct(Long productId);

    ProductDTO updateProduct(Long productId, ProductRequest request);

    ProductResponse getProductsForCurrentSeller(
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder
    );
}