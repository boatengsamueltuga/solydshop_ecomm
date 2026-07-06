package com.solydshop.ecommerce.config;

import com.solydshop.ecommerce.entity.Role;
import com.solydshop.ecommerce.repository.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the fixed set of roles (ROLE_USER, ROLE_ADMIN, ROLE_SELLER) in
 * every environment, including production - these are structural
 * reference data, not demo content, and signup/authorization depend on
 * them existing.
 */
@Component
@Order(1)
public class RoleSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;

    public RoleSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(
                    new Role(null, "ROLE_USER"),
                    new Role(null, "ROLE_ADMIN"),
                    new Role(null, "ROLE_SELLER")
            ));
            System.out.println("Roles seeded successfully.");
        }
    }
}
