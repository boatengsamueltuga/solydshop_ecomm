package com.solydshop.ecommerce.payload.response;

import java.util.List;

public class UserDTO {

    private Long userId;
    private String name;
    private String email;
    private List<String> roles;

    public UserDTO() {}

    public UserDTO(Long userId, String name, String email, List<String> roles) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.roles = roles;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
