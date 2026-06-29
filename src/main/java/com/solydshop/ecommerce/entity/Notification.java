package com.solydshop.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private boolean read = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = true)
    private Long resourceId;

    public Notification() {}

    public Notification(Long userId, String title, String message, String type) {
        this.userId    = userId;
        this.title     = title;
        this.message   = message;
        this.type      = type;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(Long userId, String title, String message, String type, Long resourceId) {
        this(userId, title, message, type);
        this.resourceId = resourceId;
    }

    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }

    public Long getUserId()                { return userId; }
    public void setUserId(Long userId)     { this.userId = userId; }

    public String getTitle()               { return title; }
    public void setTitle(String title)     { this.title = title; }

    public String getMessage()             { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType()                { return type; }
    public void setType(String type)       { this.type = type; }

    public boolean isRead()                { return read; }
    public void setRead(boolean read)      { this.read = read; }

    public LocalDateTime getCreatedAt()                    { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)      { this.createdAt = createdAt; }

    public Long getResourceId()                { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
}
