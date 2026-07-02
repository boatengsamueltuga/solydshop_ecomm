package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.entity.ApplicationStatus;
import com.solydshop.ecommerce.entity.Role;
import com.solydshop.ecommerce.entity.SellerApplication;
import com.solydshop.ecommerce.entity.User;
import com.solydshop.ecommerce.exception.ResourceNotFoundException;
import com.solydshop.ecommerce.payload.request.SellerApplicationRequest;
import com.solydshop.ecommerce.payload.response.SellerApplicationDTO;
import com.solydshop.ecommerce.repository.RoleRepository;
import com.solydshop.ecommerce.repository.SellerApplicationRepository;
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
public class SellerApplicationServiceImpl implements SellerApplicationService {

    private final SellerApplicationRepository applicationRepo;
    private final UserRepository              userRepo;
    private final RoleRepository              roleRepo;
    private final NotificationService         notificationService;

    public SellerApplicationServiceImpl(SellerApplicationRepository applicationRepo,
                                        UserRepository userRepo,
                                        RoleRepository roleRepo,
                                        NotificationService notificationService) {
        this.applicationRepo     = applicationRepo;
        this.userRepo            = userRepo;
        this.roleRepo            = roleRepo;
        this.notificationService = notificationService;
    }

    @Override
    public SellerApplicationDTO submit(String email, SellerApplicationRequest req) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Block if user already has a pending application
        applicationRepo.findTopByUserEmailOrderByCreatedAtDesc(email).ifPresent(existing -> {
            if (existing.getStatus() == ApplicationStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "You already have a pending application. Please wait for it to be reviewed.");
            }
        });

        SellerApplication app = new SellerApplication();
        app.setUser(user);
        app.setBusinessName(req.getBusinessName());
        app.setBusinessType(req.getBusinessType());
        app.setProductCategory(req.getProductCategory());
        app.setProductDescription(req.getProductDescription());
        app.setMotivation(req.getMotivation());
        app.setStatus(ApplicationStatus.PENDING);

        app = applicationRepo.save(app);

        String title   = "New seller application";
        String message = user.getName() + " (" + user.getEmail() + ") applied to sell as \""
                + app.getBusinessName() + "\".";
        Long   appId   = app.getId();

        userRepo.findByRoleName("ROLE_ADMIN").forEach(admin ->
                notificationService.createForUser(
                        admin.getUserId(), title, message, "SELLER_APPLICATION", appId));

        return SellerApplicationDTO.from(app);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerApplicationDTO getMyApplication(String email) {
        return applicationRepo.findTopByUserEmailOrderByCreatedAtDesc(email)
                .map(SellerApplicationDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("No application found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerApplicationDTO> getAllApplications() {
        return applicationRepo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(SellerApplicationDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public SellerApplicationDTO approve(Long id) {
        SellerApplication app = applicationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Application has already been reviewed.");
        }

        // Grant ROLE_SELLER to the user
        Role sellerRole = roleRepo.findByRoleName("ROLE_SELLER")
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_SELLER not found"));

        User user = app.getUser();
        Set<Role> roles = new HashSet<>(user.getRoles());
        roles.add(sellerRole);
        user.setRoles(roles);
        userRepo.save(user);

        app.setStatus(ApplicationStatus.APPROVED);
        app.setReviewedAt(LocalDateTime.now());

        SellerApplicationDTO result = SellerApplicationDTO.from(applicationRepo.save(app));

        notificationService.createForUser(
                user.getUserId(),
                "Seller Application Approved",
                "Congratulations, " + user.getName() + "! Your application for " + app.getBusinessName()
                        + " has been approved. You can now list products on SolydShop.",
                "SELLER_APPROVED",
                app.getId()
        );

        return result;
    }

    @Override
    public SellerApplicationDTO reject(Long id, String reason) {
        SellerApplication app = applicationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Application has already been reviewed.");
        }

        app.setStatus(ApplicationStatus.REJECTED);
        app.setRejectionReason(reason);
        app.setReviewedAt(LocalDateTime.now());

        SellerApplicationDTO result = SellerApplicationDTO.from(applicationRepo.save(app));

        notificationService.createForUser(
                app.getUser().getUserId(),
                "Seller Application Update",
                "Your seller application has been reviewed and was not approved at this time. "
                        + "Reason: " + reason + ". You may update your details and reapply.",
                "SELLER_REJECTED",
                app.getId()
        );

        return result;
    }
}
