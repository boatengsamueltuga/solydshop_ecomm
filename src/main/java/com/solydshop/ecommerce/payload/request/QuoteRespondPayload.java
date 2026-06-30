package com.solydshop.ecommerce.payload.request;

public class QuoteRespondPayload {
    private Double quotedPrice;
    private String sellerNote;
    private String action; // "RESPOND" or "DECLINE"

    public Double getQuotedPrice()               { return quotedPrice; }
    public void setQuotedPrice(Double p)         { this.quotedPrice = p; }

    public String getSellerNote()                { return sellerNote; }
    public void setSellerNote(String n)          { this.sellerNote = n; }

    public String getAction()                    { return action; }
    public void setAction(String action)         { this.action = action; }
}
