package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.entity.Category;
import com.solydshop.ecommerce.entity.Product;
import com.solydshop.ecommerce.entity.ProductStatus;
import com.solydshop.ecommerce.entity.User;
import com.solydshop.ecommerce.exception.ResourceNotFoundException;
import com.solydshop.ecommerce.payload.request.ForceStatusRequest;
import com.solydshop.ecommerce.payload.request.ProductRequest;
import com.solydshop.ecommerce.payload.response.AdminProductDTO;
import com.solydshop.ecommerce.payload.response.AdminProductResponse;
import com.solydshop.ecommerce.payload.response.ProductDTO;
import com.solydshop.ecommerce.payload.response.ProductResponse;
import com.solydshop.ecommerce.repository.CategoryRepository;
import com.solydshop.ecommerce.repository.ProductRepository;
import com.solydshop.ecommerce.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              UserRepository userRepository,
                              NotificationService notificationService) {
        this.productRepository    = productRepository;
        this.categoryRepository   = categoryRepository;
        this.userRepository       = userRepository;
        this.notificationService  = notificationService;
    }

    // ── Role helpers ────────────────────────────────────────────────────────

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getUserId();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // ── DTO mapping ─────────────────────────────────────────────────────────

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setModelNumber(product.getModelNumber());
        dto.setPartNumber(product.getPartNumber());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        dto.setCategoryName(product.getCategory().getCategoryName());
        dto.setCategoryId(product.getCategory().getCategoryId());
        dto.setImageUrl(product.getImageUrl());
        dto.setStatus(product.getStatus().name());
        dto.setRejectionReason(product.getRejectionReason());
        return dto;
    }

    private AdminProductDTO mapToAdminDTO(Product product) {
        AdminProductDTO dto = new AdminProductDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setModelNumber(product.getModelNumber());
        dto.setPartNumber(product.getPartNumber());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        dto.setCategoryName(product.getCategory().getCategoryName());
        dto.setCategoryId(product.getCategory().getCategoryId());
        dto.setImageUrl(product.getImageUrl());
        dto.setStatus(product.getStatus().name());
        dto.setRejectionReason(product.getRejectionReason());
        dto.setSubmittedAt(product.getSubmittedAt());
        if (product.getSeller() != null) {
            dto.setSellerId(product.getSeller().getUserId());
            dto.setSellerName(product.getSeller().getName());
            dto.setSellerEmail(product.getSeller().getEmail());
        }
        return dto;
    }

    // ── Notification helpers ─────────────────────────────────────────────────

    private void notifyAdmins(String title, String message, String type) {
        notifyAdmins(title, message, type, null);
    }

    private void notifyAdmins(String title, String message, String type, Long resourceId) {
        try {
            List<User> admins = userRepository.findByRoleName("ROLE_ADMIN");
            admins.forEach(admin ->
                    notificationService.createForUser(admin.getUserId(), title, message, type, resourceId));
        } catch (Exception e) {
            log.warn("Failed to notify admins: {}", e.getMessage());
        }
    }

    private void notifySeller(User seller, String title, String message, String type) {
        notifySeller(seller, title, message, type, null);
    }

    private void notifySeller(User seller, String title, String message, String type, Long resourceId) {
        if (seller == null) return;
        try {
            notificationService.createForUser(seller.getUserId(), title, message, type, resourceId);
        } catch (Exception e) {
            log.warn("Failed to notify seller: {}", e.getMessage());
        }
    }

    // ── Core CRUD ────────────────────────────────────────────────────────────

    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    @Override
    public ProductDTO createProduct(ProductRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setModelNumber(request.getModelNumber());
        product.setPartNumber(request.getPartNumber());
        product.setImageUrl(request.getImageUrl());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(category);

        if (isAdmin()) {
            // Platform product — no seller, goes live immediately
            product.setSeller(null);
            product.setStatus(ProductStatus.ACTIVE);
        } else {
            // Seller product — enters the review queue
            User seller = getCurrentUser();
            product.setSeller(seller);
            product.setStatus(ProductStatus.PENDING_REVIEW);
            product.setSubmittedAt(LocalDateTime.now());

            productRepository.save(product);
            notifyAdmins(
                    "New product pending review",
                    "\"" + request.getProductName() + "\" was submitted by " + seller.getName(),
                    "PRODUCT_REVIEW",
                    product.getProductId()
            );
            return mapToDTO(product);
        }

        productRepository.save(product);
        return mapToDTO(product);
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy,
                                         String sortOrder, String keyword, Long categoryId,
                                         Double maxPrice, Boolean inStock) {

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        String kw       = (keyword != null && !keyword.isBlank()) ? keyword : null;
        boolean stockOnly = Boolean.TRUE.equals(inStock);

        // Public endpoint always returns ACTIVE products only
        Page<Product> pageProducts = productRepository.findAllWithFilters(
                kw, categoryId, maxPrice, stockOnly, pageable);

        List<ProductDTO> productDTOs = pageProducts.getContent().stream()
                .map(this::mapToDTO).toList();

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
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));
        return mapToDTO(product);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    @Override
    public void deleteProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        if (!isAdmin()) {
            // Seller can only delete their own products
            if (product.getSeller() == null ||
                    !product.getSeller().getUserId().equals(getCurrentUserId())) {
                throw new RuntimeException("You are not authorized to delete this product");
            }
        }

        productRepository.delete(product);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    @Override
    public ProductDTO updateProduct(Long productId, ProductRequest request) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        boolean admin = isAdmin();

        if (!admin) {
            Long userId = getCurrentUserId();
            // Seller must own the product
            if (product.getSeller() == null ||
                    !product.getSeller().getUserId().equals(userId)) {
                throw new RuntimeException("You are not authorized to update this product");
            }
            // Seller cannot edit suspended or archived products
            if (product.getStatus() == ProductStatus.SUSPENDED ||
                    product.getStatus() == ProductStatus.ARCHIVED) {
                throw new RuntimeException(
                        "This product is " + product.getStatus().name().toLowerCase() +
                        " and cannot be edited");
            }
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        // Detect whether any content field changed (vs price/qty only)
        boolean contentChanged =
                !Objects.equals(product.getProductName(),  request.getProductName())  ||
                !Objects.equals(product.getDescription(),  request.getDescription())  ||
                !Objects.equals(product.getModelNumber(),  request.getModelNumber())  ||
                !Objects.equals(product.getPartNumber(),   request.getPartNumber())   ||
                !Objects.equals(product.getImageUrl(),     request.getImageUrl())     ||
                !product.getCategory().getCategoryId().equals(request.getCategoryId());

        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setModelNumber(request.getModelNumber());
        product.setPartNumber(request.getPartNumber());
        product.setImageUrl(request.getImageUrl());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(category);

        // Seller content edits pull the product back into the review queue
        if (!admin && contentChanged) {
            product.setStatus(ProductStatus.PENDING_REVIEW);
            product.setRejectionReason(null);
            product.setSubmittedAt(LocalDateTime.now());

            notifyAdmins(
                    "Product resubmitted for review",
                    "\"" + request.getProductName() + "\" was updated and is pending re-approval",
                    "PRODUCT_REVIEW",
                    product.getProductId()
            );
        }

        productRepository.save(product);
        return mapToDTO(product);
    }

    @PreAuthorize("hasRole('SELLER')")
    @Override
    public ProductResponse getProductsForCurrentSeller(Integer pageNumber, Integer pageSize,
                                                        String sortBy, String sortOrder) {
        Long userId = getCurrentUserId();

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Product> pageProducts = productRepository.findBySellerUserId(userId, pageable);

        List<ProductDTO> productDTOs = pageProducts.getContent().stream()
                .map(this::mapToDTO).toList();

        ProductResponse response = new ProductResponse();
        response.setContent(productDTOs);
        response.setPageNumber(pageProducts.getNumber());
        response.setPageSize(pageProducts.getSize());
        response.setTotalElements(pageProducts.getTotalElements());
        response.setTotalPages(pageProducts.getTotalPages());
        response.setLastPage(pageProducts.isLast());
        return response;
    }

    // ── Admin product management ─────────────────────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public AdminProductResponse getAdminProducts(Integer pageNumber, Integer pageSize, String sortBy,
                                                  String sortOrder, String keyword, String status) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        ProductStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = ProductStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid product status: " + status);
            }
        }

        String kw = (keyword != null && !keyword.isBlank()) ? keyword : null;

        Page<Product> pageProducts = productRepository.findAllForAdmin(statusEnum, kw, pageable);

        List<AdminProductDTO> dtos = pageProducts.getContent().stream()
                .map(this::mapToAdminDTO).toList();

        AdminProductResponse response = new AdminProductResponse();
        response.setContent(dtos);
        response.setPageNumber(pageProducts.getNumber());
        response.setPageSize(pageProducts.getSize());
        response.setTotalElements(pageProducts.getTotalElements());
        response.setTotalPages(pageProducts.getTotalPages());
        response.setLastPage(pageProducts.isLast());
        return response;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public AdminProductDTO approveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        if (product.getStatus() != ProductStatus.PENDING_REVIEW) {
            throw new RuntimeException("Only PENDING_REVIEW products can be approved");
        }

        product.setStatus(ProductStatus.ACTIVE);
        product.setRejectionReason(null);
        productRepository.save(product);

        notifySeller(product.getSeller(),
                "Product approved",
                "\"" + product.getProductName() + "\" is now live on the storefront",
                "PRODUCT_APPROVED", product.getProductId());

        return mapToAdminDTO(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public AdminProductDTO rejectProduct(Long productId, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        if (product.getStatus() != ProductStatus.PENDING_REVIEW) {
            throw new RuntimeException("Only PENDING_REVIEW products can be rejected");
        }

        product.setStatus(ProductStatus.REJECTED);
        product.setRejectionReason(reason);
        productRepository.save(product);

        notifySeller(product.getSeller(),
                "Product rejected",
                "\"" + product.getProductName() + "\" was rejected: " + reason,
                "PRODUCT_REJECTED", product.getProductId());

        return mapToAdminDTO(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public AdminProductDTO suspendProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        if (product.getStatus() == ProductStatus.ARCHIVED) {
            throw new RuntimeException("Archived products cannot be suspended");
        }

        product.setStatus(ProductStatus.SUSPENDED);
        productRepository.save(product);

        notifySeller(product.getSeller(),
                "Product suspended",
                "\"" + product.getProductName() + "\" has been suspended and is no longer visible",
                "PRODUCT_SUSPENDED", product.getProductId());

        return mapToAdminDTO(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public AdminProductDTO reinstateProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        if (product.getStatus() != ProductStatus.SUSPENDED) {
            throw new RuntimeException("Only SUSPENDED products can be reinstated");
        }

        product.setStatus(ProductStatus.ACTIVE);
        productRepository.save(product);

        notifySeller(product.getSeller(),
                "Product reinstated",
                "\"" + product.getProductName() + "\" is live again on the storefront",
                "PRODUCT_REINSTATED", product.getProductId());

        return mapToAdminDTO(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public AdminProductDTO archiveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        product.setStatus(ProductStatus.ARCHIVED);
        productRepository.save(product);

        return mapToAdminDTO(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public AdminProductDTO forceStatus(Long productId, ForceStatusRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        ProductStatus newStatus;
        try {
            newStatus = ProductStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid product status: " + request.getStatus());
        }

        if (newStatus == ProductStatus.REJECTED) {
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
                throw new RuntimeException("Rejection reason is required");
            }
            product.setRejectionReason(request.getRejectionReason());
        } else {
            product.setRejectionReason(null);
        }

        ProductStatus previous = product.getStatus();
        product.setStatus(newStatus);
        productRepository.save(product);

        // Notify seller of meaningful transitions
        if (newStatus == ProductStatus.ACTIVE && previous != ProductStatus.ACTIVE) {
            notifySeller(product.getSeller(),
                    "Product approved",
                    "\"" + product.getProductName() + "\" is now live on the storefront",
                    "PRODUCT_APPROVED", product.getProductId());
        } else if (newStatus == ProductStatus.REJECTED) {
            notifySeller(product.getSeller(),
                    "Product rejected",
                    "\"" + product.getProductName() + "\" was rejected: " + request.getRejectionReason(),
                    "PRODUCT_REJECTED", product.getProductId());
        } else if (newStatus == ProductStatus.SUSPENDED && previous != ProductStatus.SUSPENDED) {
            notifySeller(product.getSeller(),
                    "Product suspended",
                    "\"" + product.getProductName() + "\" has been suspended and is no longer visible",
                    "PRODUCT_SUSPENDED", product.getProductId());
        }

        return mapToAdminDTO(product);
    }
}
