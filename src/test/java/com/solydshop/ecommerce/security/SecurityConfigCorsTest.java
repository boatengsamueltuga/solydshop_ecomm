package com.solydshop.ecommerce.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigCorsTest {

    @Test
    void corsConfigurationSource_splitsAndTrimsCommaSeparatedOrigins() {
        SecurityConfig config = new SecurityConfig(null, null, null);
        ReflectionTestUtils.setField(config, "allowedOrigins",
                "https://solydshop.vercel.app, http://localhost:3000");

        CorsConfigurationSource source = config.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/public/products");

        CorsConfiguration resolved = source.getCorsConfiguration(request);

        assertEquals(2, resolved.getAllowedOrigins().size());
        assertTrue(resolved.getAllowedOrigins().contains("https://solydshop.vercel.app"));
        assertTrue(resolved.getAllowedOrigins().contains("http://localhost:3000"));
    }
}
