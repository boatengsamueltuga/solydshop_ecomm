package com.solydshop.ecommerce.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {

        Map<String, String> config = new HashMap<>();

        config.put("cloud_name", "REMOVED");
        config.put("api_key", "REMOVED");
        config.put("api_secret", "REMOVED");

        return new Cloudinary(config);
    }
}
