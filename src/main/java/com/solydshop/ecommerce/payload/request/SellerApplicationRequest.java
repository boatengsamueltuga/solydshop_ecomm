package com.solydshop.ecommerce.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SellerApplicationRequest {

    @NotBlank
    private String businessName;

    @NotBlank
    private String businessType;

    @NotBlank
    private String productCategory;

    @NotBlank
    @Size(min = 30, message = "Product description must be at least 30 characters")
    private String productDescription;

    @NotBlank
    @Size(min = 20, message = "Motivation must be at least 20 characters")
    private String motivation;

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
}
