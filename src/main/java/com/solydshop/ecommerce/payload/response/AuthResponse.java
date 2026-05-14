package com.solydshop.ecommerce.payload.response;

public class AuthResponse {

    private Long userId;
    private String email;

    private String accessToken;
    private String refreshToken;

    public AuthResponse() {
    }

    // Existing constructor
    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // Added constructor for /me endpoint
    public AuthResponse(Long userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}