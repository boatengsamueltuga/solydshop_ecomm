package com.solydshop.ecommerce.payload.response;

import java.util.List;

public class AuthResponse {

    private Long userId;

    private String name;

    private String email;

    // Added roles for frontend authorization
    private List<String> roles;

    private String accessToken;

    private String refreshToken;

    public AuthResponse() {
    }

    // Login response constructor
    public AuthResponse(
            String accessToken,
            String refreshToken
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // /me endpoint response constructor
    public AuthResponse(
            Long userId,
            String name,
            String email,
            List<String> roles
    ) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.roles = roles;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}