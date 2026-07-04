package com.solydshop.ecommerce.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtCookieUtilTest {

    private JwtCookieUtil newUtil(boolean secure, String sameSite) {
        JwtCookieUtil util = new JwtCookieUtil();
        ReflectionTestUtils.setField(util, "cookieSecure", secure);
        ReflectionTestUtils.setField(util, "cookieSameSite", sameSite);
        return util;
    }

    @Test
    void addAccessTokenCookie_prod_setsSecureAndSameSiteNone() {
        JwtCookieUtil util = newUtil(true, "None");
        MockHttpServletResponse response = new MockHttpServletResponse();

        util.addAccessTokenCookie(response, "test-token");

        String header = response.getHeader("Set-Cookie");
        assertTrue(header.contains("Secure"));
        assertTrue(header.contains("SameSite=None"));
        assertTrue(header.contains("HttpOnly"));
    }

    @Test
    void addAccessTokenCookie_localDev_defaultsToLaxAndNotSecure() {
        JwtCookieUtil util = newUtil(false, "Lax");
        MockHttpServletResponse response = new MockHttpServletResponse();

        util.addAccessTokenCookie(response, "test-token");

        String header = response.getHeader("Set-Cookie");
        assertTrue(header.contains("SameSite=Lax"));
        assertFalse(header.contains("Secure"));
    }

    @Test
    void clearCookies_setsMaxAgeZeroForBothCookies() {
        JwtCookieUtil util = newUtil(false, "Lax");
        MockHttpServletResponse response = new MockHttpServletResponse();

        util.clearCookies(response);

        List<String> cookies = response.getHeaders("Set-Cookie");
        assertEquals(2, cookies.size());
        assertTrue(cookies.get(0).contains("Max-Age=0"));
        assertTrue(cookies.get(1).contains("Max-Age=0"));
    }
}
