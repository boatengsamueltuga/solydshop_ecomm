package com.solydshop.ecommerce.config;

import com.solydshop.ecommerce.entity.Role;
import com.solydshop.ecommerce.entity.User;
import com.solydshop.ecommerce.repository.RoleRepository;
import com.solydshop.ecommerce.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {

        // Seed roles
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(
                    new Role(null, "ROLE_USER"),
                    new Role(null, "ROLE_ADMIN"),
                    new Role(null, "ROLE_SELLER")
            ));
            System.out.println("Roles seeded successfully.");
        }

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
