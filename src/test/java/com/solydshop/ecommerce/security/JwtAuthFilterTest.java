package com.solydshop.ecommerce.security;

import com.solydshop.ecommerce.entity.Role;
import com.solydshop.ecommerce.entity.User;
import com.solydshop.ecommerce.repository.UserRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock private UserRepository userRepository;
    @Mock private FilterChain filterChain;

    private JwtUtil jwtUtil;
    private JwtAuthFilter filter;

    private static final String SECRET = "test-secret-test-secret-test-secret-123";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "SECRET", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "EXPIRATION", 3_600_000L);

        filter = new JwtAuthFilter();
        ReflectionTestUtils.setField(filter, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(filter, "userRepository", userRepository);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User testUser() {
        User user = new User();
        user.setUserId(1L);
        user.setName("Bo Buyer");
        user.setEmail("buyer@example.com");
        user.setRoles(new HashSet<>(Set.of(new Role(1L, "ROLE_USER"))));
        return user;
    }

    private String tokenFor(User user, long expirationMillis) {
        ReflectionTestUtils.setField(jwtUtil, "EXPIRATION", expirationMillis);
        return jwtUtil.generateToken(new CustomUserDetails(user));
    }

    @Test
    void expiredToken_doesNotThrow_andProceedsUnauthenticated() throws Exception {
        String expiredToken = tokenFor(testUser(), -1000L); // already in the past

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("accessToken", expiredToken));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertDoesNotThrow(() -> filter.doFilter(request, response, filterChain));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void malformedToken_doesNotThrow_andProceedsUnauthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("accessToken", "not-a-real-jwt"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertDoesNotThrow(() -> filter.doFilter(request, response, filterChain));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void validToken_setsAuthentication() throws Exception {
        User user = testUser();
        String validToken = tokenFor(user, 3_600_000L);
        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("accessToken", validToken));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("buyer@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void noToken_proceedsUnauthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
