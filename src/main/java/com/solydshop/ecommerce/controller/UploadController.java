package com.solydshop.ecommerce.controller;

import com.solydshop.ecommerce.service.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final CloudinaryService cloudinaryService;

    public UploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file
    ) {

        String imageUrl = cloudinaryService.uploadFile(file);

        return ResponseEntity.ok(imageUrl);
    }
}