package com.solydshop.ecommerce.payload.response;

import com.solydshop.ecommerce.entity.SellerDowngradeRequest;

import java.time.LocalDateTime;

public class SellerDowngradeRequestDTO {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String reason;
    private String status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    // Populated only for the admin review queue — tells the admin why
    // approval may be blocked (409) before they click it.
    private Long openOrderCount;
    private Long pendingQuoteCount;

    public SellerDowngradeRequestDTO() {}

    public static SellerDowngradeRequestDTO from(SellerDowngradeRequest r) {
        SellerDowngradeRequestDTO dto = new SellerDowngradeRequestDTO();
        dto.id              = r.getId();
        dto.userId          = r.getUser().getUserId();
        dto.userName        = r.getUser().getName();
        dto.userEmail       = r.getUser().getEmail();
        dto.reason          = r.getReason();
        dto.status          = r.getStatus().name();
        dto.rejectionReason = r.getRejectionReason();
        dto.createdAt       = r.getCreatedAt();
        dto.reviewedAt      = r.getReviewedAt();
        return dto;
    }

    public Long getId()                    { return id; }
    public Long getUserId()                { return userId; }
    public String getUserName()            { return userName; }
    public String getUserEmail()           { return userEmail; }
    public String getReason()              { return reason; }
    public String getStatus()              { return status; }
    public String getRejectionReason()     { return rejectionReason; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
    public LocalDateTime getReviewedAt()   { return reviewedAt; }

    public Long getOpenOrderCount()                      { return openOrderCount; }
    public void setOpenOrderCount(Long openOrderCount)   { this.openOrderCount = openOrderCount; }

    public Long getPendingQuoteCount()                        { return pendingQuoteCount; }
    public void setPendingQuoteCount(Long pendingQuoteCount)  { this.pendingQuoteCount = pendingQuoteCount; }
}
