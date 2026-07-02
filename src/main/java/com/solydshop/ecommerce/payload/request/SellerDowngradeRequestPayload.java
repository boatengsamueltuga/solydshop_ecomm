package com.solydshop.ecommerce.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SellerDowngradeRequestPayload {

    @NotBlank
    @Size(min = 10, message = "Reason must be at least 10 characters")
    private String reason;

    public String getReason()               { return reason; }
    public void setReason(String reason)    { this.reason = reason; }
}
