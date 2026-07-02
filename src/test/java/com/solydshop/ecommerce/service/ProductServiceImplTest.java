package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.entity.Category;
import com.solydshop.ecommerce.entity.Product;
import com.solydshop.ecommerce.entity.ProductStatus;
import com.solydshop.ecommerce.entity.User;
import com.solydshop.ecommerce.payload.request.ForceStatusRequest;
import com.solydshop.ecommerce.repository.CartItemRepository;
import com.solydshop.ecommerce.repository.CategoryRepository;
import com.solydshop.ecommerce.repository.ProductRepository;
import com.solydshop.ecommerce.repository.QuoteRepository;
import com.solydshop.ecommerce.repository.ReviewRepository;
import com.solydshop.ecommerce.repository.UserRepository;
import com.solydshop.ecommerce.repository.WishlistItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository       productRepository;
    @Mock private CategoryRepository      categoryRepository;
    @Mock private UserRepository          userRepository;
    @Mock private NotificationService     notificationService;
    @Mock private ReviewRepository        reviewRepository;
    @Mock private QuoteRepository         quoteRepository;
    @Mock private WishlistItemRepository  wishlistItemRepository;
    @Mock private CartItemRepository      cartItemRepository;

    @InjectMocks
    private ProductServiceImpl service;

    private Product product;
    private User seller;

    @BeforeEach
    void setUp() {
        seller = new User();
        seller.setUserId(9L);

        product = new Product();
        product.setProductId(5L);
        product.setProductName("Hydraulic Pump");
        product.setSeller(seller);
        product.setStatus(ProductStatus.ACTIVE);
        product.setCategory(new Category(1L, "Hydraulics"));
    }

    @Test
    void archiveProduct_notifiesSeller() {
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        service.archiveProduct(5L);

        assertEquals(ProductStatus.ARCHIVED, product.getStatus());
        verify(notificationService).createForUser(
                eq(9L), anyString(), anyString(), eq("PRODUCT_ARCHIVED"), eq(5L));
    }

    @Test
    void forceStatus_toArchived_notifiesSeller() {
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ForceStatusRequest req = new ForceStatusRequest();
        req.setStatus("ARCHIVED");

        service.forceStatus(5L, req);

        assertEquals(ProductStatus.ARCHIVED, product.getStatus());
        verify(notificationService).createForUser(
                eq(9L), anyString(), anyString(), eq("PRODUCT_ARCHIVED"), eq(5L));
    }

    @Test
    void forceStatus_alreadyArchived_doesNotRenotify() {
        product.setStatus(ProductStatus.ARCHIVED);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ForceStatusRequest req = new ForceStatusRequest();
        req.setStatus("ARCHIVED");

        service.forceStatus(5L, req);

        verify(notificationService, never()).createForUser(any(), any(), any(), any(), any());
    }
}
