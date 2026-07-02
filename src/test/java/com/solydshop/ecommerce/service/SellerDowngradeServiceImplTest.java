package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.OrderStatus.OrderStatus;
import com.solydshop.ecommerce.entity.*;
import com.solydshop.ecommerce.exception.ResourceNotFoundException;
import com.solydshop.ecommerce.payload.request.SellerDowngradeRequestPayload;
import com.solydshop.ecommerce.payload.response.SellerDowngradeRequestDTO;
import com.solydshop.ecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerDowngradeServiceImplTest {

    @Mock private SellerDowngradeRequestRepository requestRepo;
    @Mock private UserRepository                    userRepo;
    @Mock private RoleRepository                     roleRepo;
    @Mock private ProductRepository                  productRepo;
    @Mock private OrderItemRepository                orderItemRepo;
    @Mock private QuoteRepository                    quoteRepo;
    @Mock private NotificationService                notificationService;

    @InjectMocks
    private SellerDowngradeServiceImpl service;

    private User seller;
    private Role sellerRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        sellerRole = new Role(2L, "ROLE_SELLER");
        userRole   = new Role(1L, "ROLE_USER");

        seller = new User();
        seller.setUserId(42L);
        seller.setName("Ada Seller");
        seller.setEmail("ada@example.com");
        Set<Role> roles = new HashSet<>();
        roles.add(sellerRole);
        seller.setRoles(roles);
    }

    private SellerDowngradeRequest pendingRequest() {
        SellerDowngradeRequest req = new SellerDowngradeRequest();
        req.setId(7L);
        req.setUser(seller);
        req.setReason("I no longer wish to sell on the platform.");
        req.setStatus(ApplicationStatus.PENDING);
        return req;
    }

    @Test
    void submit_rejectsNonSellers() {
        seller.setRoles(new HashSet<>(Set.of(userRole)));
        when(userRepo.findByEmail("ada@example.com")).thenReturn(Optional.of(seller));

        SellerDowngradeRequestPayload payload = new SellerDowngradeRequestPayload();
        payload.setReason("I want to stop selling.");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.submit("ada@example.com", payload));

        assertEquals(409, ex.getStatusCode().value());
        verify(requestRepo, never()).save(any());
    }

    @Test
    void submit_rejectsWhenAlreadyPending() {
        when(userRepo.findByEmail("ada@example.com")).thenReturn(Optional.of(seller));
        when(requestRepo.findTopByUserEmailOrderByCreatedAtDesc("ada@example.com"))
                .thenReturn(Optional.of(pendingRequest()));

        SellerDowngradeRequestPayload payload = new SellerDowngradeRequestPayload();
        payload.setReason("I want to stop selling.");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.submit("ada@example.com", payload));

        assertEquals(409, ex.getStatusCode().value());
        verify(requestRepo, never()).save(any());
    }

    @Test
    void submit_savesRequestAndNotifiesAllAdmins() {
        when(userRepo.findByEmail("ada@example.com")).thenReturn(Optional.of(seller));
        when(requestRepo.findTopByUserEmailOrderByCreatedAtDesc("ada@example.com"))
                .thenReturn(Optional.empty());
        when(requestRepo.save(any(SellerDowngradeRequest.class))).thenAnswer(inv -> {
            SellerDowngradeRequest r = inv.getArgument(0);
            r.setId(7L);
            return r;
        });

        User admin1 = new User(); admin1.setUserId(1L);
        User admin2 = new User(); admin2.setUserId(2L);
        when(userRepo.findByRoleName("ROLE_ADMIN")).thenReturn(List.of(admin1, admin2));

        SellerDowngradeRequestPayload payload = new SellerDowngradeRequestPayload();
        payload.setReason("I want to stop selling.");

        SellerDowngradeRequestDTO dto = service.submit("ada@example.com", payload);

        assertEquals("PENDING", dto.getStatus());
        assertEquals(7L, dto.getId());
        verify(notificationService).createForUser(eq(1L), anyString(), anyString(), eq("SELLER_DOWNGRADE_REQUEST"), eq(7L));
        verify(notificationService).createForUser(eq(2L), anyString(), anyString(), eq("SELLER_DOWNGRADE_REQUEST"), eq(7L));
    }

    @Test
    void approve_blockedByOpenOrders() {
        SellerDowngradeRequest req = pendingRequest();
        when(requestRepo.findById(7L)).thenReturn(Optional.of(req));
        when(orderItemRepo.countDistinctOpenOrdersBySeller(eq(42L), anyList())).thenReturn(2L);
        when(quoteRepo.countBySellerAndStatus(seller, QuoteStatus.PENDING)).thenReturn(0L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.approve(7L));

        assertEquals(409, ex.getStatusCode().value());
        verify(productRepo, never()).saveAll(any());
        verify(userRepo, never()).save(any());
    }

    @Test
    void approve_blockedByPendingQuotes() {
        SellerDowngradeRequest req = pendingRequest();
        when(requestRepo.findById(7L)).thenReturn(Optional.of(req));
        when(orderItemRepo.countDistinctOpenOrdersBySeller(eq(42L), anyList())).thenReturn(0L);
        when(quoteRepo.countBySellerAndStatus(seller, QuoteStatus.PENDING)).thenReturn(3L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.approve(7L));

        assertEquals(409, ex.getStatusCode().value());
        verify(productRepo, never()).saveAll(any());
    }

    @Test
    void approve_archivesProductsAndRevertsRole() {
        SellerDowngradeRequest req = pendingRequest();
        when(requestRepo.findById(7L)).thenReturn(Optional.of(req));
        when(orderItemRepo.countDistinctOpenOrdersBySeller(eq(42L), anyList())).thenReturn(0L);
        when(quoteRepo.countBySellerAndStatus(seller, QuoteStatus.PENDING)).thenReturn(0L);

        Product p1 = new Product(); p1.setProductId(1L); p1.setStatus(ProductStatus.ACTIVE);
        Product p2 = new Product(); p2.setProductId(2L); p2.setStatus(ProductStatus.ACTIVE);
        when(productRepo.findAllBySellerUserId(42L)).thenReturn(List.of(p1, p2));

        when(roleRepo.findByRoleName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(requestRepo.save(any(SellerDowngradeRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        SellerDowngradeRequestDTO dto = service.approve(7L);

        assertEquals("APPROVED", dto.getStatus());
        assertEquals(ProductStatus.ARCHIVED, p1.getStatus());
        assertEquals(ProductStatus.ARCHIVED, p2.getStatus());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userCaptor.capture());
        Set<String> savedRoleNames = userCaptor.getValue().getRoles().stream()
                .map(Role::getRoleName).collect(java.util.stream.Collectors.toSet());
        assertTrue(savedRoleNames.contains("ROLE_USER"));
        assertFalse(savedRoleNames.contains("ROLE_SELLER"));

        verify(notificationService).createForUser(eq(42L), anyString(), anyString(),
                eq("SELLER_DOWNGRADE_APPROVED"), eq(7L));
    }

    @Test
    void approve_rejectsAlreadyReviewedRequest() {
        SellerDowngradeRequest req = pendingRequest();
        req.setStatus(ApplicationStatus.APPROVED);
        when(requestRepo.findById(7L)).thenReturn(Optional.of(req));

        assertThrows(ResponseStatusException.class, () -> service.approve(7L));
        verify(orderItemRepo, never()).countDistinctOpenOrdersBySeller(anyLong(), anyList());
    }

    @Test
    void reject_marksRequestRejectedAndNotifiesUser() {
        SellerDowngradeRequest req = pendingRequest();
        when(requestRepo.findById(7L)).thenReturn(Optional.of(req));
        when(requestRepo.save(any(SellerDowngradeRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        SellerDowngradeRequestDTO dto = service.reject(7L, "Not eligible right now.");

        assertEquals("REJECTED", dto.getStatus());
        assertEquals("Not eligible right now.", dto.getRejectionReason());
        verify(notificationService).createForUser(eq(42L), anyString(), anyString(),
                eq("SELLER_DOWNGRADE_REJECTED"), eq(7L));
        verify(userRepo, never()).save(any());
    }

    @Test
    void getMyRequest_throwsWhenNoneExists() {
        when(requestRepo.findTopByUserEmailOrderByCreatedAtDesc("nobody@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getMyRequest("nobody@example.com"));
    }

    @SuppressWarnings("unchecked")
    private static List<OrderStatus> anyList() {
        return any(List.class);
    }
}
