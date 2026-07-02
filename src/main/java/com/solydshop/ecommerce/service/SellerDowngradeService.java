package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.request.SellerDowngradeRequestPayload;
import com.solydshop.ecommerce.payload.response.SellerDowngradeRequestDTO;

import java.util.List;

public interface SellerDowngradeService {
    SellerDowngradeRequestDTO submit(String email, SellerDowngradeRequestPayload payload);
    SellerDowngradeRequestDTO getMyRequest(String email);
    List<SellerDowngradeRequestDTO> getAllRequests();
    SellerDowngradeRequestDTO approve(Long id);
    SellerDowngradeRequestDTO reject(Long id, String reason);
}
