package com.solydshop.ecommerce.config;

import com.solydshop.ecommerce.entity.Role;
import com.solydshop.ecommerce.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleSeederTest {

    @Mock private RoleRepository roleRepository;

    @InjectMocks
    private RoleSeeder roleSeeder;

    @Test
    void run_seedsAllThreeRoles_whenRoleTableIsEmpty() {
        when(roleRepository.count()).thenReturn(0L);

        roleSeeder.run(null);

        ArgumentCaptor<List<Role>> captor = ArgumentCaptor.forClass(List.class);
        verify(roleRepository).saveAll(captor.capture());

        List<Role> savedRoles = captor.getValue();
        assertEquals(3, savedRoles.size());
        List<String> roleNames = savedRoles.stream().map(Role::getRoleName).toList();
        assertTrue(roleNames.contains("ROLE_USER"));
        assertTrue(roleNames.contains("ROLE_ADMIN"));
        assertTrue(roleNames.contains("ROLE_SELLER"));
    }

    @Test
    void run_doesNotSeed_whenRolesAlreadyExist() {
        when(roleRepository.count()).thenReturn(3L);

        roleSeeder.run(null);

        verify(roleRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }
}
