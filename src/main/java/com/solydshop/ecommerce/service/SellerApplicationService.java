package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.request.SellerApplicationRequest;
import com.solydshop.ecommerce.payload.response.SellerApplicationDTO;

import java.util.List;

public interface SellerApplicationService {

    SellerApplicationDTO submit(String email, SellerApplicationRequest request);

    SellerApplicationDTO getMyApplication(String email);

    List<SellerApplicationDTO> getAllApplications();

    SellerApplicationDTO approve(Long id);

    SellerApplicationDTO reject(Long id, String reason);
}
