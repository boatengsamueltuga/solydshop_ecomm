package com.solydshop.ecommerce.config;

import com.solydshop.ecommerce.entity.Role;
import com.solydshop.ecommerce.entity.User;
import com.solydshop.ecommerce.repository.RoleRepository;
import com.solydshop.ecommerce.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Seeds demo accounts (password "1234") for local development only.
 * Excluded from the "prod" profile so production never gets a known-password
 * admin account — create the real admin by signing up and granting ROLE_ADMIN
 * manually (see deployment docs).
 *
 * Roles (ROLE_USER, ROLE_ADMIN, ROLE_SELLER) are structural reference data
 * and are seeded separately by {@link RoleSeeder}, which runs in every
 * environment, including production. This class runs after RoleSeeder
 * (see @Order) so the roles it looks up already exist.
 */
@Component
@Profile("!prod")
@Order(2)
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {

        // Seed default accounts
        Role userRole   = roleRepository.findByRoleName("ROLE_USER").orElseThrow();
        Role adminRole  = roleRepository.findByRoleName("ROLE_ADMIN").orElseThrow();
        Role sellerRole = roleRepository.findByRoleName("ROLE_SELLER").orElseThrow();

        // password = 1234 (BCrypt)
        String defaultPassword = "$2a$10$GmlnkUK0bmBWerLak6nEZOj6qyjbmHcK5.wvtjAhAKYb4E0jFtHhS";

        if (!userRepository.existsByEmail("admin@mail.com")) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@mail.com");
            admin.setPassword(defaultPassword);
            admin.setRoles(new HashSet<>(Set.of(adminRole)));
            userRepository.save(admin);
            System.out.println("Admin account seeded.");
        }

        if (!userRepository.existsByEmail("seller1@mail.com")) {
            User seller1 = new User();
            seller1.setName("Seller One");
            seller1.setEmail("seller1@mail.com");
            seller1.setPassword(defaultPassword);
            seller1.setRoles(new HashSet<>(Set.of(sellerRole)));
            userRepository.save(seller1);
            System.out.println("Seller One account seeded.");
        }

        if (!userRepository.existsByEmail("seller2@mail.com")) {
            User seller2 = new User();
            seller2.setName("Seller Two");
            seller2.setEmail("seller2@mail.com");
            seller2.setPassword(defaultPassword);
            seller2.setRoles(new HashSet<>(Set.of(sellerRole)));
            userRepository.save(seller2);
            System.out.println("Seller Two account seeded.");
        }
    }
}
