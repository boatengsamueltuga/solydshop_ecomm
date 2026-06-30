package com.solydshop.ecommerce.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String productName;

    private String description;

    private String modelNumber;

    private String partNumber;

    private String imageUrl;
    private String image2Url;
    private String image3Url;
    private String image4Url;

    @NotNull(message = "Price is required")
    private double price;

    private int quantity;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getModelNumber() { return modelNumber; }
    public void setModelNumber(String modelNumber) { this.modelNumber = modelNumber; }

    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getImage2Url() { return image2Url; }
    public void setImage2Url(String image2Url) { this.image2Url = image2Url; }

    public String getImage3Url() { return image3Url; }
    public void setImage3Url(String image3Url) { this.image3Url = image3Url; }

    public String getImage4Url() { return image4Url; }
    public void setImage4Url(String image4Url) { this.image4Url = image4Url; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }


}