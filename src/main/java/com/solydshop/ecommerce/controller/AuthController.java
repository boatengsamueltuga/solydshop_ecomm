package com.solydshop.ecommerce.controller;

import com.solydshop.ecommerce.entity.Role;
import com.solydshop.ecommerce.payload.request.AuthRequest;
import com.solydshop.ecommerce.payload.request.ForgotPasswordRequest;
import com.solydshop.ecommerce.payload.request.RegisterRequest;
import com.solydshop.ecommerce.payload.request.ResetPasswordRequest;
import com.solydshop.ecommerce.payload.response.AuthResponse;
import com.solydshop.ecommerce.repository.RoleRepository;
import com.solydshop.ecommerce.repository.UserRepository;
import com.solydshop.ecommerce.security.JwtCookieUtil;
import com.solydshop.ecommerce.security.JwtUtil;
import com.solydshop.ecommerce.service.AuditLogService;
import com.solydshop.ecommerce.service.EmailService;
import com.solydshop.ecommerce.service.RateLimitService;

//import jakarta.transaction.Transactional;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.solydshop.ecommerce.payload.request.RefreshTokenRequest;
import com.solydshop.ecommerce.service.RefreshTokenService;
import com.solydshop.ecommerce.entity.RefreshToken;
import com.solydshop.ecommerce.entity.User;
import com.solydshop.ecommerce.security.CustomUserDetails;
import com.solydshop.ecommerce.repository.RefreshTokenRepository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtCookieUtil jwtCookieUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private AuditLogService auditLogService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    //This code is for setting tokens
//    @PostMapping("/login")
//    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
//
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        request.getEmail(),
//                        request.getPassword()
//                )
//        );
//
//        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//
//        String accessToken = jwtUtil.generateToken(userDetails);
//
//        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
//                userDetails.getUser().getUserId()
//        );
//
//        return ResponseEntity.ok(
//                new AuthResponse(accessToken, refreshToken.getToken())
//        );
//    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request,
                                        HttpServletRequest httpRequest,
                                        HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User loggedInUser = userDetails.getUser();

            if (loggedInUser.getFailedLoginAttempts() > 0) {
                loggedInUser.setFailedLoginAttempts(0);
                loggedInUser.setAccountLockedUntil(null);
                userRepository.save(loggedInUser);
            }

            String accessToken = jwtUtil.generateToken(userDetails);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(loggedInUser.getUserId());

            jwtCookieUtil.addAccessTokenCookie(response, accessToken);
            jwtCookieUtil.addRefreshTokenCookie(response, refreshToken.getToken());

            return ResponseEntity.ok("Login successful");

        } catch (LockedException e) {
            return ResponseEntity.status(423).body("Account locked due to too many failed attempts. Try again in 30 minutes.");
        } catch (BadCredentialsException e) {
            userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
                int attempts = user.getFailedLoginAttempts() + 1;
                user.setFailedLoginAttempts(attempts);
                if (attempts >= 5) {
                    user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
                    user.setFailedLoginAttempts(0);
                }
                userRepository.save(user);
            });
            return ResponseEntity.status(401).body("Invalid email or password.");
        }
    }

//    @PostMapping("/refresh")
//    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
//
//        RefreshToken refreshToken = refreshTokenService
//                .verifyExpiration(
//                        refreshTokenService
//                                .findByToken(request.getRefreshToken())
//                                .orElseThrow(() -> new RuntimeException("Invalid refresh token"))
//                );
//
//        User user = refreshToken.getUser();
//
//        CustomUserDetails userDetails = new CustomUserDetails(user);
//
//        String newAccessToken = jwtUtil.generateToken(userDetails);
//
//        return ResponseEntity.ok(
//                new AuthResponse(newAccessToken, request.getRefreshToken())
//        );
//    }
//    @Transactional
//    @PostMapping("/refresh")
//    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
//
//        RefreshToken refreshToken = refreshTokenService
//                .findByToken(request.getRefreshToken())
//                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
//
//        refreshTokenService.verifyExpiration(refreshToken);
//
//        User user = refreshToken.getUser();
//
//        // DELETE OLD TOKEN
//        refreshTokenRepository.delete(refreshToken);
//
//        //CREATE NEW TOKEN (rotation)
//        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getUserId());
//
//        CustomUserDetails userDetails = new CustomUserDetails(user);
//
//        String newAccessToken = jwtUtil.generateToken(userDetails);
//
//        return ResponseEntity.ok(
//                new AuthResponse(newAccessToken, newRefreshToken.getToken())
//        );
//    }


    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(HttpServletRequest request,
                                               HttpServletResponse response) {

        String refreshTokenValue = null;

        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshTokenValue = cookie.getValue();
            }
        }

        RefreshToken refreshToken = refreshTokenService
                .findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        refreshTokenRepository.delete(refreshToken);

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getUserId());

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String newAccessToken = jwtUtil.generateToken(userDetails);

        jwtCookieUtil.addAccessTokenCookie(response, newAccessToken);
        jwtCookieUtil.addRefreshTokenCookie(response, newRefreshToken.getToken());

        return ResponseEntity.ok("Token refreshed");
    }

//    @PostMapping("/logout")
//    public ResponseEntity<String> logout() {
//
//        Long userId = (Long) SecurityContextHolder
//                .getContext()
//                .getAuthentication()
//                .getCredentials();
//
//        refreshTokenService.deleteByUserId(userId);
//
//        return ResponseEntity.ok("Logged out successfully");
//    }

//    @PostMapping("/logout")
//    public ResponseEntity<String> logout(HttpServletResponse response) {
//
//        Long userId = (Long) SecurityContextHolder
//                .getContext()
//                .getAuthentication()
//                .getCredentials();
//
//        refreshTokenService.deleteByUserId(userId);
//
//        // CLEAR COOKIES
//        jwtCookieUtil.clearCookies(response);
//
//        return ResponseEntity.ok("Logged out successfully");
//    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletResponse response,
            Authentication authentication
    ) {

        if (authentication != null) {

            CustomUserDetails userDetails =
                    (CustomUserDetails) authentication.getPrincipal();

            Long userId = userDetails
                    .getUser()
                    .getUserId();

            refreshTokenService.deleteByUserId(userId);
        }

        // CLEAR COOKIES
        jwtCookieUtil.clearCookies(response);

        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(
            @RequestBody RegisterRequest request
    ) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Email already exists");
        }

        User user = new User();

        user.setName(request.getName());

        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        Role userRole = roleRepository
                .findByRoleName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request,
                                                  HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);

        if (rateLimitService.isRateLimited(request.getEmail())) {
            long minutesLeft = rateLimitService.getMinutesUntilReset(request.getEmail());
            auditLogService.log(request.getEmail(), ip, "RATE_LIMITED");
            return ResponseEntity.status(429).body(
                "Too many requests. Please try again in " + minutesLeft + " minute" + (minutesLeft == 1 ? "" : "s") + "."
            );
        }

        rateLimitService.recordAttempt(request.getEmail());

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.ok("If that email exists, a reset link has been sent.");
        }

        User user = optionalUser.get();

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        auditLogService.log(user.getEmail(), ip, "REQUEST_MADE");

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), token, frontendUrl);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
        }

        return ResponseEntity.ok("If that email exists, a reset link has been sent.");
    }

    @Transactional
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request,
                                                 HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);

        Optional<User> optionalUser = userRepository.findByResetPasswordToken(request.getToken());

        if (optionalUser.isEmpty()) {
            auditLogService.log("unknown", ip, "TOKEN_INVALID");
            return ResponseEntity.badRequest().body("Invalid or expired reset token.");
        }

        User user = optionalUser.get();

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            auditLogService.log(user.getEmail(), ip, "TOKEN_EXPIRED");
            return ResponseEntity.badRequest().body("Invalid or expired reset token.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetTokenExpiry(null);
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userRepository.save(user);

        auditLogService.log(user.getEmail(), ip, "RESET_SUCCESS");

        try {
            emailService.sendPasswordChangeConfirmationEmail(user.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }

        return ResponseEntity.ok("Password reset successfully.");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            Authentication authentication
    ) {

        if (authentication == null) {

            return ResponseEntity
                    .status(401)
                    .body("Unauthorized");
        }
                CustomUserDetails userDetails =
                        (CustomUserDetails) authentication.getPrincipal();

                User user = userDetails.getUser();


        return ResponseEntity.ok(
                new AuthResponse(
                        user.getUserId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRoles()
                                .stream()
                                .map(role -> role.getRoleName())
                                .toList()
                )
        );
    }
}
