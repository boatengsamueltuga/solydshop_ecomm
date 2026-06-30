package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.request.QuoteRequestPayload;
import com.solydshop.ecommerce.payload.request.QuoteRespondPayload;
import com.solydshop.ecommerce.payload.response.QuoteDTO;

import java.util.List;

public interface QuoteService {
    QuoteDTO submitQuote(String buyerEmail, QuoteRequestPayload payload);
    List<QuoteDTO> getBuyerQuotes(String buyerEmail);
    List<QuoteDTO> getSellerQuotes(String sellerEmail);
    QuoteDTO respondToQuote(Long quoteId, String sellerEmail, QuoteRespondPayload payload);
    List<QuoteDTO> getAllQuotes();
}
