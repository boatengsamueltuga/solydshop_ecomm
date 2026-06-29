package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.request.ForceStatusRequest;
import com.solydshop.ecommerce.payload.request.ProductRequest;
import com.solydshop.ecommerce.payload.response.AdminProductDTO;
import com.solydshop.ecommerce.payload.response.AdminProductResponse;
import com.solydshop.ecommerce.payload.response.ProductDTO;
import com.solydshop.ecommerce.payload.response.ProductResponse;

public interface ProductService {

    ProductDTO createProduct(ProductRequest request);

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

    // Admin-only operations
    AdminProductResponse getAdminProducts(
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder,
            String keyword,
            String status
    );

    AdminProductDTO approveProduct(Long productId);

    AdminProductDTO rejectProduct(Long productId, String reason);

    AdminProductDTO suspendProduct(Long productId);

    AdminProductDTO reinstateProduct(Long productId);

    AdminProductDTO archiveProduct(Long productId);

    AdminProductDTO forceStatus(Long productId, ForceStatusRequest request);
}
