package com.solydshop.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seller_applications")
public class SellerApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false)
    private String businessType;

    @Column(nullable = false)
    private String productCategory;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String productDescription;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String motivation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;

    public SellerApplication() {}

    public Long getId()                                     { return id; }
    public void setId(Long id)                             { this.id = id; }

    public User getUser()                                   { return user; }
    public void setUser(User user)                         { this.user = user; }

    public String getBusinessName()                         { return businessName; }
    public void setBusinessName(String businessName)       { this.businessName = businessName; }

    public String getBusinessType()                         { return businessType; }
    public void setBusinessType(String businessType)       { this.businessType = businessType; }

    public String getProductCategory()                      { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }

    public String getProductDescription()                   { return productDescription; }
    public void setProductDescription(String d)            { this.productDescription = d; }

    public String getMotivation()                           { return motivation; }
    public void setMotivation(String motivation)           { this.motivation = motivation; }

    public ApplicationStatus getStatus()                    { return status; }
    public void setStatus(ApplicationStatus status)        { this.status = status; }

    public String getRejectionReason()                      { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt()                     { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)      { this.createdAt = createdAt; }

    public LocalDateTime getReviewedAt()                    { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt)    { this.reviewedAt = reviewedAt; }
}
