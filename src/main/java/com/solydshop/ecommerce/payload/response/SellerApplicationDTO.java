package com.solydshop.ecommerce.payload.response;

import com.solydshop.ecommerce.entity.SellerApplication;

import java.time.LocalDateTime;

public class SellerApplicationDTO {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String businessName;
    private String businessType;
    private String productCategory;
    private String productDescription;
    private String motivation;
    private String status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    public SellerApplicationDTO() {}

    public static SellerApplicationDTO from(SellerApplication a) {
        SellerApplicationDTO dto = new SellerApplicationDTO();
        dto.id                 = a.getId();
        dto.userId             = a.getUser().getUserId();
        dto.userName           = a.getUser().getName();
        dto.userEmail          = a.getUser().getEmail();
        dto.businessName       = a.getBusinessName();
        dto.businessType       = a.getBusinessType();
        dto.productCategory    = a.getProductCategory();
        dto.productDescription = a.getProductDescription();
        dto.motivation         = a.getMotivation();
        dto.status             = a.getStatus().name();
        dto.rejectionReason    = a.getRejectionReason();
        dto.createdAt          = a.getCreatedAt();
        dto.reviewedAt         = a.getReviewedAt();
        return dto;
    }

    public Long getId()                  { return id; }
    public Long getUserId()              { return userId; }
    public String getUserName()          { return userName; }
    public String getUserEmail()         { return userEmail; }
    public String getBusinessName()      { return businessName; }
    public String getBusinessType()      { return businessType; }
    public String getProductCategory()   { return productCategory; }
    public String getProductDescription(){ return productDescription; }
    public String getMotivation()        { return motivation; }
    public String getStatus()            { return status; }
    public String getRejectionReason()   { return rejectionReason; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
}
