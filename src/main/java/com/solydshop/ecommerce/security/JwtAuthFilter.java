package com.solydshop.ecommerce.security;

import com.solydshop.ecommerce.entity.User;
import com.solydshop.ecommerce.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // Added UserRepository
    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;
        String username = null;

        // READ TOKEN FROM COOKIES INSTEAD OF HEADER
        if (request.getCookies() != null) {

            for (Cookie cookie : request.getCookies()) {

                if ("accessToken".equals(cookie.getName())) {

                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null) {

            try {
                username = jwtUtil.extractUsername(token);
            } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
                // Expired, malformed, or otherwise invalid token -- fall through as
                // unauthenticated rather than letting the exception escape the filter
                // chain. Filters run before Spring MVC's @RestControllerAdvice layer,
                // so an uncaught exception here would never reach GlobalExceptionHandler;
                // it would surface as a raw 500 instead of a clean "please log in again".
            }
        }

        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtUtil.validateToken(token, username)) {

                // Load full user from database
                User user = userRepository
                        .findByEmail(username)
                        .orElse(null);

                if (user != null) {

                    CustomUserDetails customUserDetails =
                            new CustomUserDetails(user);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    customUserDetails,
                                    null,
                                    customUserDetails.getAuthorities()
                            );

                    // Added request details
                    authToken.setDetails(
                            new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}