package com.solydshop.ecommerce.payload.response;

import java.time.LocalDateTime;

public class AdminProductDTO {

    private Long productId;
    private String productName;
    private String description;
    private String modelNumber;
    private String partNumber;
    private double price;
    private int quantity;
    private String categoryName;
    private Long categoryId;
    private String imageUrl;
    private String image2Url;
    private String image3Url;
    private String image4Url;
    private String status;
    private String rejectionReason;
    private LocalDateTime submittedAt;
    private Long sellerId;
    private String sellerName;
    private String sellerEmail;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getModelNumber() { return modelNumber; }
    public void setModelNumber(String modelNumber) { this.modelNumber = modelNumber; }

    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getImage2Url() { return image2Url; }
    public void setImage2Url(String image2Url) { this.image2Url = image2Url; }

    public String getImage3Url() { return image3Url; }
    public void setImage3Url(String image3Url) { this.image3Url = image3Url; }

    public String getImage4Url() { return image4Url; }
    public void setImage4Url(String image4Url) { this.image4Url = image4Url; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerEmail() { return sellerEmail; }
    public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }
}
