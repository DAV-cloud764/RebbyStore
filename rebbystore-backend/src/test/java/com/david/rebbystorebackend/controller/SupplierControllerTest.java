package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.domain.entity.Supplier;
import com.david.rebbystorebackend.repository.SupplierRepository;
import com.david.rebbystorebackend.security.UserPrincipal;
import com.david.rebbystorebackend.security.jwt.JwtService;
import com.david.rebbystorebackend.security.service.CustomUserDetailsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SupplierControllerTest {

    private static final String STAFF_TOKEN = "staff-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupplierRepository supplierRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        supplierRepository.deleteAll();

        UserPrincipal staffPrincipal = UserPrincipal.createForTesting(
                2L,
                "staff",
                "staff@rebbystore.co.tz",
                "$2a$10$dummy",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_STAFF"))
        );

        when(jwtService.isTokenValid(STAFF_TOKEN))
                .thenReturn(true);

        when(jwtService.extractUsername(STAFF_TOKEN))
                .thenReturn(staffPrincipal.getUsername());

        when(userDetailsService.loadUserByUsername("staff"))
                .thenReturn(staffPrincipal);
    }

    private MockHttpServletRequestBuilder authenticated(
            MockHttpServletRequestBuilder request
    ) {
        return request.header(
                "Authorization",
                "Bearer " + STAFF_TOKEN
        );
    }

    @Test
    void shouldCreateSupplier() throws Exception {

        String requestBody = """
                {
                  "name": "Beauty Hair Suppliers",
                  "phone": "+255712345678",
                  "email": "BEAUTY@EXAMPLE.COM",
                  "address": "Dar es Salaam"
                }
                """;

        mockMvc.perform(
                        authenticated(
                                post("/api/suppliers")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name")
                        .value("Beauty Hair Suppliers"))
                .andExpect(jsonPath("$.phone")
                        .value("0712345678"))
                .andExpect(jsonPath("$.email")
                        .value("beauty@example.com"))
                .andExpect(jsonPath("$.address")
                        .value("Dar es Salaam"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldGetSupplierById() throws Exception {

        Supplier supplier = createSupplier(
                "Mambo Hair",
                "0711111111",
                "mambo@example.com",
                "Dar es Salaam"
        );

        mockMvc.perform(
                        authenticated(
                                get("/api/suppliers/{id}", supplier.getId())
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(supplier.getId()))
                .andExpect(jsonPath("$.name")
                        .value("Mambo Hair"))
                .andExpect(jsonPath("$.phone")
                        .value("0711111111"))
                .andExpect(jsonPath("$.email")
                        .value("mambo@example.com"))
                .andExpect(jsonPath("$.address")
                        .value("Dar es Salaam"));
    }

    @Test
    void shouldGetSupplierByName() throws Exception {

        createSupplier(
                "Premium Wigs",
                "0722222222",
                "premium@example.com",
                "Arusha"
        );

        mockMvc.perform(
                        authenticated(
                                get("/api/suppliers/name/{name}", "Premium Wigs")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Premium Wigs"))
                .andExpect(jsonPath("$.phone")
                        .value("0722222222"));
    }

    @Test
    void shouldGetAllSuppliersOrderedByName() throws Exception {

        createSupplier(
                "Zulu Hair",
                "0733333333",
                "zulu@example.com",
                "Dar es Salaam"
        );

        createSupplier(
                "Alpha Hair",
                "0744444444",
                "alpha@example.com",
                "Dodoma"
        );

        mockMvc.perform(
                        authenticated(
                                get("/api/suppliers")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name")
                        .value("Alpha Hair"))
                .andExpect(jsonPath("$[1].name")
                        .value("Zulu Hair"));
    }

    @Test
    void shouldUpdateSupplier() throws Exception {

        Supplier supplier = createSupplier(
                "Old Supplier",
                "0755555555",
                "old@example.com",
                "Mwanza"
        );

        String requestBody = """
                {
                  "name": "Updated Supplier",
                  "phone": "+255756789012",
                  "email": "UPDATED@EXAMPLE.COM",
                  "address": "Dodoma"
                }
                """;

        mockMvc.perform(
                        authenticated(
                                put("/api/suppliers/{id}", supplier.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Updated Supplier"))
                .andExpect(jsonPath("$.phone")
                        .value("0756789012"))
                .andExpect(jsonPath("$.email")
                        .value("updated@example.com"))
                .andExpect(jsonPath("$.address")
                        .value("Dodoma"));
    }

    @Test
    void shouldDeleteSupplier() throws Exception {

        Supplier supplier = createSupplier(
                "Delete Me",
                "0766666666",
                "delete@example.com",
                "Dar es Salaam"
        );

        mockMvc.perform(
                        authenticated(
                                delete("/api/suppliers/{id}", supplier.getId())
                        )
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        authenticated(
                                get("/api/suppliers/{id}", supplier.getId())
                        )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectInvalidCreateRequest() throws Exception {

        String requestBody = """
                {
                  "name": "",
                  "phone": "12345",
                  "email": "not-an-email",
                  "address": "Dar es Salaam"
                }
                """;

        mockMvc.perform(
                        authenticated(
                                post("/api/suppliers")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.phone").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void shouldRejectDuplicateSupplierName() throws Exception {

        createSupplier(
                "Unique Supplier",
                "0777777777",
                "one@example.com",
                "Dar es Salaam"
        );

        String requestBody = """
                {
                  "name": "Unique Supplier",
                  "phone": "0788888888",
                  "email": "two@example.com",
                  "address": "Arusha"
                }
                """;

        mockMvc.perform(
                        authenticated(
                                post("/api/suppliers")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Supplier with name 'Unique Supplier' already exists"
                        ));
    }

    @Test
    void shouldReturnNotFoundWhenSupplierDoesNotExist() throws Exception {

        mockMvc.perform(
                        authenticated(
                                get("/api/suppliers/{id}", 999999L)
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Supplier with id 999999 not found"));
    }

    @Test
    void shouldReturnNotFoundWhenSupplierNameDoesNotExist() throws Exception {

        mockMvc.perform(
                        authenticated(
                                get(
                                        "/api/suppliers/name/{name}",
                                        "Missing Supplier"
                                )
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Supplier with name 'Missing Supplier' not found"
                        ));
    }

    @Test
    void shouldAllowOptionalContactFields() throws Exception {

        String requestBody = """
                {
                  "name": "No Contact Supplier",
                  "phone": null,
                  "email": null,
                  "address": null
                }
                """;

        mockMvc.perform(
                        authenticated(
                                post("/api/suppliers")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name")
                        .value("No Contact Supplier"))
                .andExpect(jsonPath("$.phone")
                        .value(is((String) null)))
                .andExpect(jsonPath("$.email")
                        .value(is((String) null)))
                .andExpect(jsonPath("$.address")
                        .value(is((String) null)));
    }

    private Supplier createSupplier(
            String name,
            String phone,
            String email,
            String address
    ) {
        OffsetDateTime now = OffsetDateTime.now();

        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setPhone(phone);
        supplier.setEmail(email);
        supplier.setAddress(address);
        supplier.setCreatedAt(now);
        supplier.setUpdatedAt(now);

        return supplierRepository.save(supplier);
    }
}