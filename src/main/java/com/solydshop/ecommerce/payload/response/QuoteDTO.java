package com.solydshop.ecommerce.payload.response;

import java.time.LocalDateTime;

public class QuoteDTO {
    private Long   quoteId;
    private Long   productId;
    private String productName;
    private String productImageUrl;
    private String productPartNumber;
    private Long   buyerId;
    private String buyerName;
    private String buyerEmail;
    private Long   sellerId;
    private String sellerName;
    private int    qtyNeeded;
    private String urgency;
    private String notes;
    private String contactEmail;
    private String phone;
    private String status;
    private Double quotedPrice;
    private String sellerNote;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    public Long   getQuoteId()                         { return quoteId; }
    public void   setQuoteId(Long quoteId)             { this.quoteId = quoteId; }

    public Long   getProductId()                       { return productId; }
    public void   setProductId(Long productId)         { this.productId = productId; }

    public String getProductName()                     { return productName; }
    public void   setProductName(String productName)   { this.productName = productName; }

    public String getProductImageUrl()                       { return productImageUrl; }
    public void   setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }

    public String getProductPartNumber()                          { return productPartNumber; }
    public void   setProductPartNumber(String productPartNumber)  { this.productPartNumber = productPartNumber; }

    public Long   getBuyerId()                         { return buyerId; }
    public void   setBuyerId(Long buyerId)             { this.buyerId = buyerId; }

    public String getBuyerName()                       { return buyerName; }
    public void   setBuyerName(String buyerName)       { this.buyerName = buyerName; }

    public String getBuyerEmail()                      { return buyerEmail; }
    public void   setBuyerEmail(String buyerEmail)     { this.buyerEmail = buyerEmail; }

    public Long   getSellerId()                        { return sellerId; }
    public void   setSellerId(Long sellerId)           { this.sellerId = sellerId; }

    public String getSellerName()                      { return sellerName; }
    public void   setSellerName(String sellerName)     { this.sellerName = sellerName; }

    public int    getQtyNeeded()                       { return qtyNeeded; }
    public void   setQtyNeeded(int qtyNeeded)          { this.qtyNeeded = qtyNeeded; }

    public String getUrgency()                         { return urgency; }
    public void   setUrgency(String urgency)           { this.urgency = urgency; }

    public String getNotes()                           { return notes; }
    public void   setNotes(String notes)               { this.notes = notes; }

    public String getContactEmail()                    { return contactEmail; }
    public void   setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getPhone()                           { return phone; }
    public void   setPhone(String phone)               { this.phone = phone; }

    public String getStatus()                          { return status; }
    public void   setStatus(String status)             { this.status = status; }

    public Double getQuotedPrice()                     { return quotedPrice; }
    public void   setQuotedPrice(Double quotedPrice)   { this.quotedPrice = quotedPrice; }

    public String getSellerNote()                      { return sellerNote; }
    public void   setSellerNote(String sellerNote)     { this.sellerNote = sellerNote; }

    public LocalDateTime getCreatedAt()                          { return createdAt; }
    public void          setCreatedAt(LocalDateTime createdAt)   { this.createdAt = createdAt; }

    public LocalDateTime getRespondedAt()                            { return respondedAt; }
    public void          setRespondedAt(LocalDateTime respondedAt)   { this.respondedAt = respondedAt; }
}
