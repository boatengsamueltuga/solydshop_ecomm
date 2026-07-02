package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.entity.*;
import com.solydshop.ecommerce.payload.request.SellerApplicationRequest;
import com.solydshop.ecommerce.payload.response.SellerApplicationDTO;
import com.solydshop.ecommerce.repository.RoleRepository;
import com.solydshop.ecommerce.repository.SellerApplicationRepository;
import com.solydshop.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerApplicationServiceImplTest {

    @Mock private SellerApplicationRepository applicationRepo;
    @Mock private UserRepository              userRepo;
    @Mock private RoleRepository               roleRepo;
    @Mock private NotificationService          notificationService;

    @InjectMocks
    private SellerApplicationServiceImpl service;

    private User buyer;

    @BeforeEach
    void setUp() {
        buyer = new User();
        buyer.setUserId(10L);
        buyer.setName("Bo Buyer");
        buyer.setEmail("bo@example.com");
        buyer.setRoles(new HashSet<>());
    }

    private SellerApplicationRequest request() {
        SellerApplicationRequest req = new SellerApplicationRequest();
        req.setBusinessName("Bo's Hydraulics");
        req.setBusinessType("SOLE_TRADER");
        req.setProductCategory("Hydraulic parts");
        req.setProductDescription("We supply hydraulic pumps and seal kits for heavy equipment.");
        req.setMotivation("Ten years in the industry, want to reach more buyers.");
        return req;
    }

    @Test
    void submit_notifiesAllAdmins() {
        when(userRepo.findByEmail("bo@example.com")).thenReturn(Optional.of(buyer));
        when(applicationRepo.findTopByUserEmailOrderByCreatedAtDesc("bo@example.com"))
                .thenReturn(Optional.empty());
        when(applicationRepo.save(any(SellerApplication.class))).thenAnswer(inv -> {
            SellerApplication a = inv.getArgument(0);
            a.setId(5L);
            return a;
        });

        User admin1 = new User(); admin1.setUserId(1L);
        User admin2 = new User(); admin2.setUserId(2L);
        when(userRepo.findByRoleName("ROLE_ADMIN")).thenReturn(List.of(admin1, admin2));

        SellerApplicationDTO dto = service.submit("bo@example.com", request());

        assertEquals("PENDING", dto.getStatus());
        assertEquals(5L, dto.getId());
        verify(notificationService).createForUser(eq(1L), anyString(), anyString(), eq("SELLER_APPLICATION"), eq(5L));
        verify(notificationService).createForUser(eq(2L), anyString(), anyString(), eq("SELLER_APPLICATION"), eq(5L));
    }

    @Test
    void submit_blocksWhenAlreadyPending() {
        when(userRepo.findByEmail("bo@example.com")).thenReturn(Optional.of(buyer));

        SellerApplication pending = new SellerApplication();
        pending.setStatus(ApplicationStatus.PENDING);
        when(applicationRepo.findTopByUserEmailOrderByCreatedAtDesc("bo@example.com"))
                .thenReturn(Optional.of(pending));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.submit("bo@example.com", request()));

        assertEquals(409, ex.getStatusCode().value());
        verify(applicationRepo, never()).save(any());
        verify(notificationService, never()).createForUser(any(), any(), any(), any(), any());
    }

    @Test
    void submit_allowsReapplyAfterApproved() {
        when(userRepo.findByEmail("bo@example.com")).thenReturn(Optional.of(buyer));

        SellerApplication approved = new SellerApplication();
        approved.setStatus(ApplicationStatus.APPROVED);
        when(applicationRepo.findTopByUserEmailOrderByCreatedAtDesc("bo@example.com"))
                .thenReturn(Optional.of(approved));
        when(applicationRepo.save(any(SellerApplication.class))).thenAnswer(inv -> {
            SellerApplication a = inv.getArgument(0);
            a.setId(6L);
            return a;
        });
        when(userRepo.findByRoleName("ROLE_ADMIN")).thenReturn(List.of());

        SellerApplicationDTO dto = service.submit("bo@example.com", request());

        assertEquals("PENDING", dto.getStatus());
        verify(applicationRepo).save(any(SellerApplication.class));
    }

    @Test
    void approve_notifiesOnlyTheApplicant_notAdmins() {
        SellerApplication app = new SellerApplication();
        app.setId(5L);
        app.setUser(buyer);
        app.setBusinessName("Bo's Hydraulics");
        app.setStatus(ApplicationStatus.PENDING);
        when(applicationRepo.findById(5L)).thenReturn(Optional.of(app));
        when(applicationRepo.save(any(SellerApplication.class))).thenAnswer(inv -> inv.getArgument(0));

        Role sellerRole = new Role(3L, "ROLE_SELLER");
        when(roleRepo.findByRoleName("ROLE_SELLER")).thenReturn(Optional.of(sellerRole));

        SellerApplicationDTO dto = service.approve(5L);

        assertEquals("APPROVED", dto.getStatus());
        verify(notificationService).createForUser(eq(10L), anyString(), anyString(), eq("SELLER_APPROVED"), eq(5L));
        verify(userRepo, never()).findByRoleName("ROLE_ADMIN");
    }
}
