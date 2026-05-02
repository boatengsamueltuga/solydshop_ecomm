package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.payload.request.ProductRequest;
import com.solydshop.ecommerce.payload.response.ProductDTO;

public interface ProductService {

    ProductDTO createProduct(ProductRequest request);
}