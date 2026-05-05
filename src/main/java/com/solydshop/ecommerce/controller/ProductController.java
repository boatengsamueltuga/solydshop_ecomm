package com.solydshop.ecommerce.controller;

import com.solydshop.ecommerce.payload.request.ProductRequest;
import com.solydshop.ecommerce.payload.response.ProductDTO;
import com.solydshop.ecommerce.payload.response.ProductResponse;
import com.solydshop.ecommerce.service.ProductService;
import com.solydshop.ecommerce.util.AppConstants;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/admin/products")
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody ProductRequest request) {

        ProductDTO dto = productService.createProduct(request);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_DIR) String sortOrder

    ) {
        ProductResponse response = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/products/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @DeleteMapping("/admin/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/products/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest request
    ) {
        ProductDTO dto = productService.updateProduct(id, request);
        return ResponseEntity.ok(dto);
    }
}