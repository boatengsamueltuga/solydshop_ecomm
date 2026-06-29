package com.solydshop.ecommerce.controller;

import com.solydshop.ecommerce.payload.request.ForceStatusRequest;
import com.solydshop.ecommerce.payload.request.ProductRequest;
import com.solydshop.ecommerce.payload.request.RejectRequest;
import com.solydshop.ecommerce.payload.response.AdminProductDTO;
import com.solydshop.ecommerce.payload.response.AdminProductResponse;
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

    // ── Public ───────────────────────────────────────────────────────────────

    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER)      Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE)        Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String  sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_DIR)String  sortOrder,
            @RequestParam(required = false) String  keyword,
            @RequestParam(required = false) Long    categoryId,
            @RequestParam(required = false) Double  maxPrice,
            @RequestParam(required = false) Boolean inStock) {

        return ResponseEntity.ok(productService.getAllProducts(
                pageNumber, pageSize, sortBy, sortOrder,
                keyword, categoryId, maxPrice, inStock));
    }

    @GetMapping("/public/products/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // ── Seller ────────────────────────────────────────────────────────────────

    @PostMapping("/seller/products")
    public ResponseEntity<ProductDTO> createProductAsSeller(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @PutMapping("/seller/products/{id}")
    public ResponseEntity<ProductDTO> updateProductAsSeller(
            @PathVariable Long id,
            @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/seller/products/{id}")
    public ResponseEntity<Void> deleteProductAsSeller(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/seller/products")
    public ResponseEntity<ProductResponse> getSellerProducts(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER)      Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE)        Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String  sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_DIR)String  sortOrder) {

        return ResponseEntity.ok(productService.getProductsForCurrentSeller(
                pageNumber, pageSize, sortBy, sortOrder));
    }

    // ── Admin — CRUD ──────────────────────────────────────────────────────────

    @GetMapping("/admin/products")
    public ResponseEntity<AdminProductResponse> getAdminProducts(
            @RequestParam(defaultValue = "0")           Integer pageNumber,
            @RequestParam(defaultValue = "1000")        Integer pageSize,
            @RequestParam(defaultValue = "productId")   String  sortBy,
            @RequestParam(defaultValue = "desc")        String  sortOrder,
            @RequestParam(required = false)             String  keyword,
            @RequestParam(required = false)             String  status) {

        return ResponseEntity.ok(productService.getAdminProducts(
                pageNumber, pageSize, sortBy, sortOrder, keyword, status));
    }

    @PostMapping("/admin/products")
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @PutMapping("/admin/products/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/admin/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // ── Admin — Status actions ────────────────────────────────────────────────

    @PostMapping("/admin/products/{id}/approve")
    public ResponseEntity<AdminProductDTO> approveProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.approveProduct(id));
    }

    @PostMapping("/admin/products/{id}/reject")
    public ResponseEntity<AdminProductDTO> rejectProduct(
            @PathVariable Long id,
            @RequestBody RejectRequest request) {
        return ResponseEntity.ok(productService.rejectProduct(id, request.getReason()));
    }

    @PostMapping("/admin/products/{id}/suspend")
    public ResponseEntity<AdminProductDTO> suspendProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.suspendProduct(id));
    }

    @PostMapping("/admin/products/{id}/reinstate")
    public ResponseEntity<AdminProductDTO> reinstateProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.reinstateProduct(id));
    }

    @PostMapping("/admin/products/{id}/archive")
    public ResponseEntity<AdminProductDTO> archiveProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.archiveProduct(id));
    }

    @PostMapping("/admin/products/{id}/force-status")
    public ResponseEntity<AdminProductDTO> forceStatus(
            @PathVariable Long id,
            @RequestBody ForceStatusRequest request) {
        return ResponseEntity.ok(productService.forceStatus(id, request));
    }
}
