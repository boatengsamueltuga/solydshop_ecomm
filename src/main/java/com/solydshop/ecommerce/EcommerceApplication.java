package com.solydshop.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcommerceApplication {

	public static void main(String[] args) {

		System.out.println("APP STARTED FRESH");
		//System.out.println(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("1234"));
		SpringApplication.run(EcommerceApplication.class, args);
	}

}
