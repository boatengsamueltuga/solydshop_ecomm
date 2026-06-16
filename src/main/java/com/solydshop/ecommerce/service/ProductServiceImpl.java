package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.entity.Category;
import com.solydshop.ecommerce.entity.Product;
import com.solydshop.ecommerce.entity.User;
import com.solydshop.ecommerce.exception.ResourceNotFoundException;
import com.solydshop.ecommerce.payload.request.ProductRequest;
import com.solydshop.ecommerce.payload.response.ProductDTO;
import com.solydshop.ecommerce.payload.response.ProductResponse;
import com.solydshop.ecommerce.repository.CategoryRepository;
import com.solydshop.ecommerce.repository.ProductRepository;
import com.solydshop.ecommerce.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              UserRepository userRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    private ProductDTO mapToDTO(Product product) {

        ProductDTO dto = new ProductDTO();

        dto.setProductId(product.getProductId());

        dto.setProductName(product.getProductName());

        dto.setDescription(product.getDescription());

        dto.setModelNumber(product.getModelNumber());

        dto.setPartNumber(product.getPartNumber());

        dto.setPrice(product.getPrice());

        dto.setQuantity(product.getQuantity());

        dto.setCategoryName(
                product.getCategory()
                        .getCategoryName()
        );

        // Added categoryId mapping
        dto.setCategoryId(
                product.getCategory()
                        .getCategoryId()
        );

        // Added imageUrl mapping
        dto.setImageUrl(
                product.getImageUrl()
        );

        return dto;
    }

//    // NEW METHOD (uses JWT userId from credentials)
//    private Long getCurrentUserId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        return (Long) authentication.getCredentials();
//    }

    // Get currently authenticated user by email
    private Long getCurrentUserId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        return user.getUserId();
    }

    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    @Override
    public ProductDTO createProduct(ProductRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id: " + request.getCategoryId())
                );

        Long userId = getCurrentUserId();

        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setModelNumber(request.getModelNumber());
        product.setPartNumber(request.getPartNumber());
        product.setImageUrl(request.getImageUrl());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(category);
        product.setSeller(seller);

        productRepository.save(product);

        return mapToDTO(product);
    }

    @Override
    public ProductResponse getAllProducts(
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder,
            String keyword,
            Long categoryId,
            Double maxPrice
    ) {

        Sort sort = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                sort
        );

        Page<Product> pageProducts;

        boolean hasKeyword  = keyword != null && !keyword.isBlank();
        boolean hasCategory = categoryId != null;
        boolean hasPrice    = maxPrice != null;

        if (hasKeyword && hasCategory && hasPrice) {
            pageProducts = productRepository.searchByKeywordAndCategoryWithMaxPrice(keyword, categoryId, maxPrice, pageable);
        } else if (hasKeyword && hasCategory) {
            pageProducts = productRepository.searchByKeywordAndCategory(keyword, categoryId, pageable);
        } else if (hasKeyword && hasPrice) {
            pageProducts = productRepository.searchByKeywordWithMaxPrice(keyword, maxPrice, pageable);
        } else if (hasKeyword) {
            pageProducts = productRepository.searchByKeyword(keyword, pageable);
        } else if (hasCategory && hasPrice) {
            pageProducts = productRepository.findByCategoryCategoryIdAndPriceLessThanEqual(categoryId, maxPrice, pageable);
        } else if (hasCategory) {
            pageProducts = productRepository.findByCategoryCategoryId(categoryId, pageable);
        } else if (hasPrice) {
            pageProducts = productRepository.findByPriceLessThanEqual(maxPrice, pageable);
        } else {
            pageProducts = productRepository.findAll(pageable);
        }

        List<ProductDTO> productDTOs =
                pageProducts.getContent()
                        .stream()
                        .map(this::mapToDTO)
                        .toList();

        ProductResponse response = new ProductResponse();

        response.setContent(productDTOs);
        response.setPageNumber(pageProducts.getNumber());
        response.setPageSize(pageProducts.getSize());
        response.setTotalElements(pageProducts.getTotalElements());
        response.setTotalPages(pageProducts.getTotalPages());
        response.setLastPage(pageProducts.isLast());

        return response;
    }

    @Override
    public ProductDTO getProductById(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + productId)
                );

        return mapToDTO(product);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    @Override
    public void deleteProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + productId
                        )
                );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        boolean isAdmin = authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );

        Long userId = getCurrentUserId();

    /*
    ---------------------------------------------------------------
    | Sellers Can Only Delete Their Own Products
    | Admins Can Delete Any Product
    ---------------------------------------------------------------
    */

        if (!isAdmin &&
                !product.getSeller().getUserId().equals(userId)) {

            throw new RuntimeException(
                    "You are not authorized to delete this product"
            );
        }

        productRepository.delete(product);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    @Override
    public ProductDTO updateProduct(Long productId, ProductRequest request) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + productId)
                );

        Long userId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        boolean isAdmin = user.getRoles()
                .stream()
                .anyMatch(role ->
                        role.getRoleName()
                                .equals("ROLE_ADMIN")
                );

        if (
                !isAdmin &&
                        !product.getSeller().getUserId().equals(userId)
        ) {

            throw new RuntimeException(
                    "You are not authorized to update this product"
            );
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id: " + request.getCategoryId())
                );

        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setModelNumber(request.getModelNumber());
        product.setPartNumber(request.getPartNumber());
        product.setImageUrl(request.getImageUrl());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(category);

        productRepository.save(product);

        return mapToDTO(product);
    }

    @PreAuthorize("hasRole('SELLER')")
    @Override
    public ProductResponse getProductsForCurrentSeller(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Long userId = getCurrentUserId();

        Sort sort = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> pageProducts = productRepository.findBySellerUserId(userId, pageable);

        List<ProductDTO> productDTOs = pageProducts.getContent()
                .stream()
                .map(this::mapToDTO)
                .toList();

        ProductResponse response = new ProductResponse();
        response.setContent(productDTOs);
        response.setPageNumber(pageProducts.getNumber());
        response.setPageSize(pageProducts.getSize());
        response.setTotalElements(pageProducts.getTotalElements());
        response.setTotalPages(pageProducts.getTotalPages());
        response.setLastPage(pageProducts.isLast());

        return response;
    }
}