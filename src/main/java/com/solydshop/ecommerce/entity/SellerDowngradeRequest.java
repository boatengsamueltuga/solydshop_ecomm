package com.solydshop.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seller_downgrade_requests")
public class SellerDowngradeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;

    public SellerDowngradeRequest() {}

    public Long getId()                                     { return id; }
    public void setId(Long id)                             { this.id = id; }

    public User getUser()                                   { return user; }
    public void setUser(User user)                         { this.user = user; }

    public String getReason()                               { return reason; }
    public void setReason(String reason)                   { this.reason = reason; }

    public ApplicationStatus getStatus()                    { return status; }
    public void setStatus(ApplicationStatus status)        { this.status = status; }

    public String getRejectionReason()                      { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt()                     { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)      { this.createdAt = createdAt; }

    public LocalDateTime getReviewedAt()                    { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt)    { this.reviewedAt = reviewedAt; }
}
