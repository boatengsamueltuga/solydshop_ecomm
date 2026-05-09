//package com.solydshop.ecommerce.payload.response;
//
//public class AuthResponse {
//
//    private String token;
//
//    public AuthResponse() {}
//
//    public AuthResponse(String token) {
//        this.token = token;
//    }
//
//    public String getToken() {
//        return token;
//    }
//
//    public void setToken(String token) {
//        this.token = token;
//    }
//}

package com.solydshop.ecommerce.payload.response;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
}
