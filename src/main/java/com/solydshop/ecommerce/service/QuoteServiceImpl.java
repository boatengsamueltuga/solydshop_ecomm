package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.entity.*;
import com.solydshop.ecommerce.exception.ResourceNotFoundException;
import com.solydshop.ecommerce.payload.request.QuoteRequestPayload;
import com.solydshop.ecommerce.payload.request.QuoteRespondPayload;
import com.solydshop.ecommerce.payload.response.QuoteDTO;
import com.solydshop.ecommerce.repository.ProductRepository;
import com.solydshop.ecommerce.repository.QuoteRepository;
import com.solydshop.ecommerce.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository     quoteRepository;
    private final ProductRepository   productRepository;
    private final UserRepository      userRepository;
    private final NotificationService notificationService;

    public QuoteServiceImpl(QuoteRepository quoteRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository,
                            NotificationService notificationService) {
        this.quoteRepository     = quoteRepository;
        this.productRepository   = productRepository;
        this.userRepository      = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public QuoteDTO submitQuote(String buyerEmail, QuoteRequestPayload payload) {
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(payload.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        QuoteRequest quote = new QuoteRequest();
        quote.setBuyer(buyer);
        quote.setProduct(product);
        quote.setSeller(product.getSeller());
        quote.setQtyNeeded(payload.getQtyNeeded());
        quote.setUrgency(payload.getUrgency());
        quote.setNotes(payload.getNotes());
        quote.setContactEmail(payload.getContactEmail());
        quote.setPhone(payload.getPhone());
        quote.setStatus(QuoteStatus.PENDING);

        quote = quoteRepository.save(quote);

        String notifTitle   = "New quote request";
        String notifMessage = buyer.getName() + " requested a quote for \""
                + product.getProductName() + "\" (qty: " + payload.getQtyNeeded() + ")";
        Long   quoteId      = quote.getQuoteId();

        if (product.getSeller() != null) {
            notificationService.createForUser(
                    product.getSeller().getUserId(), notifTitle, notifMessage,
                    "QUOTE_REQUEST", quoteId);
        } else {
            // Platform product — notify every admin
            userRepository.findByRoleName("ROLE_ADMIN").forEach(admin ->
                    notificationService.createForUser(
                            admin.getUserId(), notifTitle, notifMessage,
                            "QUOTE_REQUEST", quoteId));
        }

        return mapToDTO(quote);
    }

    @Override
    public List<QuoteDTO> getBuyerQuotes(String buyerEmail) {
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return quoteRepository.findByBuyerOrderByCreatedAtDesc(buyer)
                .stream().map(this::mapToDTO).toList();
    }

    @Override
    public List<QuoteDTO> getSellerQuotes(String sellerEmail) {
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return quoteRepository.findBySellerOrderByCreatedAtDesc(seller)
                .stream().map(this::mapToDTO).toList();
    }

    @Override
    public QuoteDTO respondToQuote(Long quoteId, String sellerEmail, QuoteRespondPayload payload) {
        QuoteRequest quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found"));
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (quote.getSeller() == null || !quote.getSeller().getUserId().equals(seller.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your quote");
        }

        boolean decline = "DECLINE".equalsIgnoreCase(payload.getAction());
        quote.setStatus(decline ? QuoteStatus.DECLINED : QuoteStatus.RESPONDED);
        quote.setQuotedPrice(decline ? null : payload.getQuotedPrice());
        quote.setSellerNote(payload.getSellerNote());
        quote.setRespondedAt(LocalDateTime.now());
        quote = quoteRepository.save(quote);

        String title   = decline ? "Quote declined" : "Quote received";
        String message = decline
                ? "Your quote for \"" + quote.getProduct().getProductName() + "\" was declined by the seller."
                : seller.getName() + " responded to your quote for \"" + quote.getProduct().getProductName()
                        + "\" — quoted price: $" + String.format("%.2f", payload.getQuotedPrice());

        notificationService.createForUser(
                quote.getBuyer().getUserId(), title, message, "QUOTE_RESPONSE", quoteId);

        return mapToDTO(quote);
    }

    @Override
    public QuoteDTO respondToQuoteAsAdmin(Long quoteId, QuoteRespondPayload payload) {
        QuoteRequest quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found"));

        boolean decline = "DECLINE".equalsIgnoreCase(payload.getAction());
        quote.setStatus(decline ? QuoteStatus.DECLINED : QuoteStatus.RESPONDED);
        quote.setQuotedPrice(decline ? null : payload.getQuotedPrice());
        quote.setSellerNote(payload.getSellerNote());
        quote.setRespondedAt(LocalDateTime.now());
        quote = quoteRepository.save(quote);

        String title   = decline ? "Quote declined" : "Quote received";
        String message = decline
                ? "Your quote request for \"" + quote.getProduct().getProductName()
                        + "\" was declined."
                : "Your quote for \"" + quote.getProduct().getProductName()
                        + "\" has been answered — quoted price: $"
                        + String.format("%.2f", payload.getQuotedPrice());

        notificationService.createForUser(
                quote.getBuyer().getUserId(), title, message, "QUOTE_RESPONSE", quoteId);

        return mapToDTO(quote);
    }

    @Override
    public List<QuoteDTO> getAllQuotes() {
        return quoteRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::mapToDTO).toList();
    }

    private QuoteDTO mapToDTO(QuoteRequest q) {
        QuoteDTO dto = new QuoteDTO();
        dto.setQuoteId(q.getQuoteId());
        dto.setProductId(q.getProduct().getProductId());
        dto.setProductName(q.getProduct().getProductName());
        dto.setProductImageUrl(q.getProduct().getImageUrl());
        dto.setProductPartNumber(q.getProduct().getPartNumber());
        dto.setBuyerId(q.getBuyer().getUserId());
        dto.setBuyerName(q.getBuyer().getName());
        dto.setBuyerEmail(q.getBuyer().getEmail());
        if (q.getSeller() != null) {
            dto.setSellerId(q.getSeller().getUserId());
            dto.setSellerName(q.getSeller().getName());
        }
        dto.setQtyNeeded(q.getQtyNeeded());
        dto.setUrgency(q.getUrgency());
        dto.setNotes(q.getNotes());
        dto.setContactEmail(q.getContactEmail());
        dto.setPhone(q.getPhone());
        dto.setStatus(q.getStatus().name());
        dto.setQuotedPrice(q.getQuotedPrice());
        dto.setSellerNote(q.getSellerNote());
        dto.setCreatedAt(q.getCreatedAt());
        dto.setRespondedAt(q.getRespondedAt());
        return dto;
    }
}
