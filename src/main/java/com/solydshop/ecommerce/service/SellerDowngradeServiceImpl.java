package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.OrderStatus.OrderStatus;
import com.solydshop.ecommerce.entity.*;
import com.solydshop.ecommerce.exception.ResourceNotFoundException;
import com.solydshop.ecommerce.payload.request.SellerDowngradeRequestPayload;
import com.solydshop.ecommerce.payload.response.SellerDowngradeRequestDTO;
import com.solydshop.ecommerce.repository.OrderItemRepository;
import com.solydshop.ecommerce.repository.ProductRepository;
import com.solydshop.ecommerce.repository.QuoteRepository;
import com.solydshop.ecommerce.repository.RoleRepository;
import com.solydshop.ecommerce.repository.SellerDowngradeRequestRepository;
import com.solydshop.ecommerce.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class SellerDowngradeServiceImpl implements SellerDowngradeService {

    private static final List<OrderStatus> OPEN_ORDER_STATUSES =
            List.of(OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.SHIPPED);

    private final SellerDowngradeRequestRepository requestRepo;
    private final UserRepository                   userRepo;
    private final RoleRepository                    roleRepo;
    private final ProductRepository                 productRepo;
    private final OrderItemRepository               orderItemRepo;
    private final QuoteRepository                   quoteRepo;
    private final NotificationService                notificationService;

    public SellerDowngradeServiceImpl(SellerDowngradeRequestRepository requestRepo,
                                       UserRepository userRepo,
                                       RoleRepository roleRepo,
                                       ProductRepository productRepo,
                                       OrderItemRepository orderItemRepo,
                                       QuoteRepository quoteRepo,
                                       NotificationService notificationService) {
        this.requestRepo         = requestRepo;
        this.userRepo            = userRepo;
        this.roleRepo            = roleRepo;
        this.productRepo         = productRepo;
        this.orderItemRepo       = orderItemRepo;
        this.quoteRepo           = quoteRepo;
        this.notificationService = notificationService;
    }

    @Override
    public SellerDowngradeRequestDTO submit(String email, SellerDowngradeRequestPayload payload) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isSeller = user.getRoles() != null &&
                user.getRoles().stream().anyMatch(r -> "ROLE_SELLER".equals(r.getRoleName()));
        if (!isSeller) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only seller accounts can request to revert to a buyer account.");
        }

        requestRepo.findTopByUserEmailOrderByCreatedAtDesc(email).ifPresent(existing -> {
            if (existing.getStatus() == ApplicationStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "You already have a pending downgrade request. Please wait for it to be reviewed.");
            }
        });

        SellerDowngradeRequest req = new SellerDowngradeRequest();
        req.setUser(user);
        req.setReason(payload.getReason());
        req.setStatus(ApplicationStatus.PENDING);
        req = requestRepo.save(req);

        String title   = "Seller downgrade requested";
        String message = user.getName() + " (" + user.getEmail()
                + ") has requested to revert to a buyer account.";
        Long   reqId   = req.getId();

        userRepo.findByRoleName("ROLE_ADMIN").forEach(admin ->
                notificationService.createForUser(
                        admin.getUserId(), title, message, "SELLER_DOWNGRADE_REQUEST", reqId));

        return SellerDowngradeRequestDTO.from(req);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerDowngradeRequestDTO getMyRequest(String email) {
        return requestRepo.findTopByUserEmailOrderByCreatedAtDesc(email)
                .map(SellerDowngradeRequestDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("No downgrade request found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerDowngradeRequestDTO> getAllRequests() {
        return requestRepo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTOWithBlockingCounts)
                .collect(Collectors.toList());
    }

    @Override
    public SellerDowngradeRequestDTO approve(Long id) {
        SellerDowngradeRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Downgrade request not found"));

        if (req.getStatus() != ApplicationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Request has already been reviewed.");
        }

        User user     = req.getUser();
        Long sellerId = user.getUserId();

        long openOrders    = orderItemRepo.countDistinctOpenOrdersBySeller(sellerId, OPEN_ORDER_STATUSES);
        long pendingQuotes = quoteRepo.countBySellerAndStatus(user, QuoteStatus.PENDING);

        if (openOrders > 0 || pendingQuotes > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot approve: seller has " + openOrders + " open order(s) and "
                            + pendingQuotes + " pending quote(s) that must be resolved first.");
        }

        // Archive all of the seller's product listings — preserves order/quote
        // history and lets them be restored if the user re-applies to sell.
        List<Product> products = productRepo.findAllBySellerUserId(sellerId);
        products.forEach(p -> p.setStatus(ProductStatus.ARCHIVED));
        productRepo.saveAll(products);

        // Strip ROLE_SELLER, keep everything else (e.g. ROLE_ADMIN), ensure ROLE_USER.
        Role userRole = roleRepo.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_USER not found"));
        Set<Role> roles = user.getRoles() == null
                ? new HashSet<>()
                : user.getRoles().stream()
                        .filter(r -> !"ROLE_SELLER".equals(r.getRoleName()))
                        .collect(Collectors.toCollection(HashSet::new));
        roles.add(userRole);
        user.setRoles(roles);
        userRepo.save(user);

        req.setStatus(ApplicationStatus.APPROVED);
        req.setReviewedAt(LocalDateTime.now());
        req = requestRepo.save(req);

        notificationService.createForUser(
                user.getUserId(),
                "Seller account reverted",
                "Your account has been reverted to a standard buyer account. Your product listings "
                        + "have been archived. You can reapply to sell anytime from your account page.",
                "SELLER_DOWNGRADE_APPROVED",
                req.getId());

        return SellerDowngradeRequestDTO.from(req);
    }

    @Override
    public SellerDowngradeRequestDTO reject(Long id, String reason) {
        SellerDowngradeRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Downgrade request not found"));

        if (req.getStatus() != ApplicationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Request has already been reviewed.");
        }

        req.setStatus(ApplicationStatus.REJECTED);
        req.setRejectionReason(reason);
        req.setReviewedAt(LocalDateTime.now());
        req = requestRepo.save(req);

        notificationService.createForUser(
                req.getUser().getUserId(),
                "Downgrade request declined",
                "Your request to revert to a buyer account was declined. Reason: " + reason,
                "SELLER_DOWNGRADE_REJECTED",
                req.getId());

        return SellerDowngradeRequestDTO.from(req);
    }

    private SellerDowngradeRequestDTO toDTOWithBlockingCounts(SellerDowngradeRequest r) {
        SellerDowngradeRequestDTO dto = SellerDowngradeRequestDTO.from(r);
        if (r.getStatus() == ApplicationStatus.PENDING) {
            Long sellerId = r.getUser().getUserId();
            dto.setOpenOrderCount(orderItemRepo.countDistinctOpenOrdersBySeller(sellerId, OPEN_ORDER_STATUSES));
            dto.setPendingQuoteCount(quoteRepo.countBySellerAndStatus(r.getUser(), QuoteStatus.PENDING));
        }
        return dto;
    }
}
