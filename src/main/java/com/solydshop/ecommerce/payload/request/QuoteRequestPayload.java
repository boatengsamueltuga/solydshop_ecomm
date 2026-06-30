package com.solydshop.ecommerce.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class QuoteRequestPayload {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull @Min(1)
    private int qtyNeeded;

    @NotBlank
    private String urgency;

    private String notes;

    @NotBlank
    private String contactEmail;

    private String phone;

    public Long getProductId()                       { return productId; }
    public void setProductId(Long productId)         { this.productId = productId; }

    public int getQtyNeeded()                        { return qtyNeeded; }
    public void setQtyNeeded(int qtyNeeded)          { this.qtyNeeded = qtyNeeded; }

    public String getUrgency()                       { return urgency; }
    public void setUrgency(String urgency)           { this.urgency = urgency; }

    public String getNotes()                         { return notes; }
    public void setNotes(String notes)               { this.notes = notes; }

    public String getContactEmail()                  { return contactEmail; }
    public void setContactEmail(String e)            { this.contactEmail = e; }

    public String getPhone()                         { return phone; }
    public void setPhone(String phone)               { this.phone = phone; }
}
