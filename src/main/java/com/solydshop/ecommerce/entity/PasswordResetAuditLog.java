package com.solydshop.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_audit_log")
public class PasswordResetAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String ipAddress;

    private String event;

    private LocalDateTime timestamp;

    public PasswordResetAuditLog() {}

    public PasswordResetAuditLog(String email, String ipAddress, String event) {
        this.email = email;
        this.ipAddress = ipAddress;
        this.event = event;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getIpAddress() { return ipAddress; }
    public String getEvent() { return event; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
