package com.solydshop.ecommerce.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class JwtCookieUtil {

    private final int ACCESS_TOKEN_EXPIRY = 60 * 60; // 1 hour
    private final int REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60; // 7 days

    public void addAccessTokenCookie(HttpServletResponse response, String token) {

        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // change to true in production (HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge(ACCESS_TOKEN_EXPIRY);

        response.addCookie(cookie);
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token) {

        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // change to true in production
        cookie.setPath("/api/auth/refresh");
        cookie.setMaxAge(REFRESH_TOKEN_EXPIRY);

        response.addCookie(cookie);
    }

    public void clearCookies(HttpServletResponse response) {

        Cookie access = new Cookie("accessToken", null);
        access.setHttpOnly(true);
        access.setPath("/");
        access.setMaxAge(0);

        Cookie refresh = new Cookie("refreshToken", null);
        refresh.setHttpOnly(true);
        refresh.setPath("/api/auth/refresh");
        refresh.setMaxAge(0);

        response.addCookie(access);
        response.addCookie(refresh);
    }
}
