package com.solydshop.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quote_requests")
public class QuoteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quoteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @Column(nullable = false)
    private int qtyNeeded;

    @Column(nullable = false)
    private String urgency;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private String contactEmail;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteStatus status = QuoteStatus.PENDING;

    private Double quotedPrice;

    @Column(columnDefinition = "TEXT")
    private String sellerNote;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime respondedAt;

    public QuoteRequest() {}

    public Long getQuoteId()                         { return quoteId; }
    public void setQuoteId(Long quoteId)             { this.quoteId = quoteId; }

    public User getBuyer()                           { return buyer; }
    public void setBuyer(User buyer)                 { this.buyer = buyer; }

    public Product getProduct()                      { return product; }
    public void setProduct(Product product)          { this.product = product; }

    public User getSeller()                          { return seller; }
    public void setSeller(User seller)               { this.seller = seller; }

    public int getQtyNeeded()                        { return qtyNeeded; }
    public void setQtyNeeded(int qtyNeeded)          { this.qtyNeeded = qtyNeeded; }

    public String getUrgency()                       { return urgency; }
    public void setUrgency(String urgency)           { this.urgency = urgency; }

    public String getNotes()                         { return notes; }
    public void setNotes(String notes)               { this.notes = notes; }

    public String getContactEmail()                  { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getPhone()                         { return phone; }
    public void setPhone(String phone)               { this.phone = phone; }

    public QuoteStatus getStatus()                   { return status; }
    public void setStatus(QuoteStatus status)        { this.status = status; }

    public Double getQuotedPrice()                   { return quotedPrice; }
    public void setQuotedPrice(Double quotedPrice)   { this.quotedPrice = quotedPrice; }

    public String getSellerNote()                    { return sellerNote; }
    public void setSellerNote(String sellerNote)     { this.sellerNote = sellerNote; }

    public LocalDateTime getCreatedAt()                        { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)          { this.createdAt = createdAt; }

    public LocalDateTime getRespondedAt()                      { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt)      { this.respondedAt = respondedAt; }
}
