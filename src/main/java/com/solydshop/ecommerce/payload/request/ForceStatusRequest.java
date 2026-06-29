package com.solydshop.ecommerce.payload.request;

public class ForceStatusRequest {

    private String status;
    private String rejectionReason;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
