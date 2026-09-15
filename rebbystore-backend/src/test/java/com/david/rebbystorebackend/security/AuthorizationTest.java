package com.david.rebbystorebackend.security;

import com.david.rebbystorebackend.security.jwt.JwtService;
import com.david.rebbystorebackend.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectUnauthenticatedInventoryRequest() throws Exception {
        mockMvc.perform(get("/api/inventory/test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerShouldBeForbiddenFromInventory() throws Exception {
        configureValidToken(
                "customer-token",
                createPrincipal(
                        3L,
                        "customer",
                        "customer@rebbystore.co.tz",
                        "ROLE_CUSTOMER"
                )
        );

        mockMvc.perform(
                        get("/api/inventory/test")
                                .header("Authorization", "Bearer customer-token")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void staffShouldAccessInventory() throws Exception {
        configureValidToken(
                "staff-token",
                createPrincipal(
                        2L,
                        "staff",
                        "staff@rebbystore.co.tz",
                        "ROLE_STAFF"
                )
        );

        mockMvc.perform(
                        get("/api/inventory/test")
                                .header("Authorization", "Bearer staff-token")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("staff"));
    }

    @Test
    void adminShouldAccessInventory() throws Exception {
        configureValidToken(
                "admin-token",
                createPrincipal(
                        1L,
                        "admin",
                        "admin@rebbystore.co.tz",
                        "ROLE_ADMIN"
                )
        );

        mockMvc.perform(
                        get("/api/inventory/test")
                                .header("Authorization", "Bearer admin-token")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("admin"));
    }

    @Test
    void customerShouldNotAccessPurchaseEndpoints() throws Exception {
        configureValidToken(
                "customer-token",
                createPrincipal(
                        3L,
                        "customer",
                        "customer@rebbystore.co.tz",
                        "ROLE_CUSTOMER"
                )
        );

        mockMvc.perform(
                        get("/api/purchases/test")
                                .header("Authorization", "Bearer customer-token")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void staffShouldAccessPurchaseEndpoints() throws Exception {
        configureValidToken(
                "staff-token",
                createPrincipal(
                        2L,
                        "staff",
                        "staff@rebbystore.co.tz",
                        "ROLE_STAFF"
                )
        );

        mockMvc.perform(
                        get("/api/purchases/test")
                                .header("Authorization", "Bearer staff-token")
                )
                .andExpect(status().isOk());
    }

    @Test
    void customerShouldNotAccessSupplierEndpoints() throws Exception {
        configureValidToken(
                "customer-token",
                createPrincipal(
                        3L,
                        "customer",
                        "customer@rebbystore.co.tz",
                        "ROLE_CUSTOMER"
                )
        );

        mockMvc.perform(
                        get("/api/suppliers/test")
                                .header("Authorization", "Bearer customer-token")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void staffShouldAccessSupplierEndpoints() throws Exception {
        configureValidToken(
                "staff-token",
                createPrincipal(
                        2L,
                        "staff",
                        "staff@rebbystore.co.tz",
                        "ROLE_STAFF"
                )
        );

        mockMvc.perform(
                        get("/api/suppliers/test")
                                .header("Authorization", "Bearer staff-token")
                )
                .andExpect(status().isOk());
    }

    @Test
    void customerShouldNotAccessCustomerManagementEndpoints() throws Exception {
        configureValidToken(
                "customer-token",
                createPrincipal(
                        3L,
                        "customer",
                        "customer@rebbystore.co.tz",
                        "ROLE_CUSTOMER"
                )
        );

        mockMvc.perform(
                        get("/api/customers/test")
                                .header("Authorization", "Bearer customer-token")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void staffShouldAccessCustomerManagementEndpoints() throws Exception {
        configureValidToken(
                "staff-token",
                createPrincipal(
                        2L,
                        "staff",
                        "staff@rebbystore.co.tz",
                        "ROLE_STAFF"
                )
        );

        mockMvc.perform(
                        get("/api/customers/test")
                                .header("Authorization", "Bearer staff-token")
                )
                .andExpect(status().isOk());
    }

    @Test
    void customerShouldNotAccessReportingEndpoints() throws Exception {
        configureValidToken(
                "customer-token",
                createPrincipal(
                        3L,
                        "customer",
                        "customer@rebbystore.co.tz",
                        "ROLE_CUSTOMER"
                )
        );

        mockMvc.perform(
                        get("/api/reporting/test")
                                .header("Authorization", "Bearer customer-token")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void staffShouldAccessReportingEndpoints() throws Exception {
        configureValidToken(
                "staff-token",
                createPrincipal(
                        2L,
                        "staff",
                        "staff@rebbystore.co.tz",
                        "ROLE_STAFF"
                )
        );

        mockMvc.perform(
                        get("/api/reporting/test")
                                .header("Authorization", "Bearer staff-token")
                )
                .andExpect(status().isOk());
    }

    @Test
    void customerShouldAccessOrderEndpoints() throws Exception {
        configureValidToken(
                "customer-token",
                createPrincipal(
                        3L,
                        "customer",
                        "customer@rebbystore.co.tz",
                        "ROLE_CUSTOMER"
                )
        );

        mockMvc.perform(
                        get("/api/orders/test")
                                .header("Authorization", "Bearer customer-token")
                )
                .andExpect(status().isOk());
    }

    @Test
    void staffShouldAccessOrderEndpoints() throws Exception {
        configureValidToken(
                "staff-token",
                createPrincipal(
                        2L,
                        "staff",
                        "staff@rebbystore.co.tz",
                        "ROLE_STAFF"
                )
        );

        mockMvc.perform(
                        get("/api/orders/test")
                                .header("Authorization", "Bearer staff-token")
                )
                .andExpect(status().isOk());
    }

    @Test
    void adminShouldAccessOrderEndpoints() throws Exception {
        configureValidToken(
                "admin-token",
                createPrincipal(
                        1L,
                        "admin",
                        "admin@rebbystore.co.tz",
                        "ROLE_ADMIN"
                )
        );

        mockMvc.perform(
                        get("/api/orders/test")
                                .header("Authorization", "Bearer admin-token")
                )
                .andExpect(status().isOk());
    }

    private UserPrincipal createPrincipal(
            Long id,
            String username,
            String email,
            String role
    ) {
        return UserPrincipal.createForTesting(
                id,
                username,
                email,
                "$2a$10$dummy",
                true,
                List.of(new SimpleGrantedAuthority(role))
        );
    }

    private void configureValidToken(
            String token,
            UserPrincipal principal
    ) {
        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.extractUsername(token))
                .thenReturn(principal.getUsername());

        when(userDetailsService.loadUserByUsername(
                principal.getUsername()
        )).thenReturn(principal);
    }
}