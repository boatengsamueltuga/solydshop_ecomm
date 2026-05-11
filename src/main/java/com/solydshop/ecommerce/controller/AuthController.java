package com.solydshop.ecommerce.controller;

import com.solydshop.ecommerce.entity.Role;
import com.solydshop.ecommerce.payload.request.AuthRequest;
import com.solydshop.ecommerce.payload.request.RegisterRequest;
import com.solydshop.ecommerce.payload.response.AuthResponse;
import com.solydshop.ecommerce.repository.RoleRepository;
import com.solydshop.ecommerce.repository.UserRepository;
import com.solydshop.ecommerce.security.JwtCookieUtil;
import com.solydshop.ecommerce.security.JwtUtil;

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

import java.util.Set;

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
                                        HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtUtil.generateToken(userDetails);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                userDetails.getUser().getUserId()
        );

        //SET COOKIES INSTEAD OF RETURNING TOKENS
        jwtCookieUtil.addAccessTokenCookie(response, accessToken);
        jwtCookieUtil.addRefreshTokenCookie(response, refreshToken.getToken());

        return ResponseEntity.ok("Login successful");
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

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {

        Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getCredentials();

        refreshTokenService.deleteByUserId(userId);

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

        user.setRoles(Set.of(userRole));

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }
}
