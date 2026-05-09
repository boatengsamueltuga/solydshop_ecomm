package com.solydshop.ecommerce.controller;

import com.solydshop.ecommerce.payload.request.AuthRequest;
import com.solydshop.ecommerce.payload.response.AuthResponse;
import com.solydshop.ecommerce.security.JwtUtil;

//import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.solydshop.ecommerce.payload.request.RefreshTokenRequest;
import com.solydshop.ecommerce.service.RefreshTokenService;
import com.solydshop.ecommerce.entity.RefreshToken;
import com.solydshop.ecommerce.entity.User;
import com.solydshop.ecommerce.security.CustomUserDetails;
import com.solydshop.ecommerce.repository.RefreshTokenRepository;
import org.springframework.transaction.annotation.Transactional;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {

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

        return ResponseEntity.ok(
                new AuthResponse(accessToken, refreshToken.getToken())
        );
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
    @Transactional
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenService
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        // DELETE OLD TOKEN
        refreshTokenRepository.delete(refreshToken);

        //CREATE NEW TOKEN (rotation)
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getUserId());

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String newAccessToken = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(
                new AuthResponse(newAccessToken, newRefreshToken.getToken())
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {

        Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getCredentials();

        refreshTokenService.deleteByUserId(userId);

        return ResponseEntity.ok("Logged out successfully");
    }
}
