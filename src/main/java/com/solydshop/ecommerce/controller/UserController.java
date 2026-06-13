package com.solydshop.ecommerce.controller;

import com.solydshop.ecommerce.entity.Role;
import com.solydshop.ecommerce.entity.User;
import com.solydshop.ecommerce.exception.ResourceNotFoundException;
import com.solydshop.ecommerce.payload.response.UserDTO;
import com.solydshop.ecommerce.repository.RoleRepository;
import com.solydshop.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userRepository.findAll().stream()
                .map(user -> new UserDTO(
                        user.getUserId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRoles().stream()
                                .map(role -> role.getRoleName())
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserDTO> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role newRole = roleRepository.findByRoleName(role)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + role));

        Set<Role> roles = new HashSet<>();
        roles.add(newRole);
        user.setRoles(roles);
        userRepository.save(user);

        UserDTO dto = new UserDTO(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toList())
        );

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/users/{id}/unlock")
    public ResponseEntity<String> unlockUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userRepository.save(user);
        return ResponseEntity.ok("User account unlocked successfully");
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}
