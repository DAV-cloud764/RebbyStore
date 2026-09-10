package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.domain.entity.Customer;
import com.david.rebbystorebackend.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = createCustomer(
                "David Philanthropist",
                "0712345678",
                "david@example.com"
        );
    }

    @Test
    void shouldCreateCustomer() throws Exception {
        String requestBody = """
                {
                  "fullName": "John Doe",
                  "phone": "0712345679",
                  "email": "john@example.com"
                }
                """;

        mockMvc.perform(
                        post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.phone").value("0712345679"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.totalOrders").value(0))
                .andExpect(jsonPath("$.totalSpent").value(0))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldGetCustomerById() throws Exception {
        mockMvc.perform(
                        get("/api/customers/{id}", customer.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId()))
                .andExpect(jsonPath("$.fullName")
                        .value("David Philanthropist"))
                .andExpect(jsonPath("$.phone").value("0712345678"))
                .andExpect(jsonPath("$.email").value("david@example.com"))
                .andExpect(jsonPath("$.totalOrders").value(0))
                .andExpect(jsonPath("$.totalSpent").value(0));
    }

    @Test
    void shouldGetCustomerByPhone() throws Exception {
        mockMvc.perform(
                        get("/api/customers/phone/{phone}",
                                "+255712345678")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId()))
                .andExpect(jsonPath("$.fullName")
                        .value("David Philanthropist"))
                .andExpect(jsonPath("$.phone").value("0712345678"))
                .andExpect(jsonPath("$.email").value("david@example.com"));
    }

    @Test
    void shouldGetCustomerByEmail() throws Exception {
        mockMvc.perform(
                        get("/api/customers/email/{email}",
                                "DAVID@EXAMPLE.COM")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId()))
                .andExpect(jsonPath("$.fullName")
                        .value("David Philanthropist"))
                .andExpect(jsonPath("$.phone").value("0712345678"))
                .andExpect(jsonPath("$.email").value("david@example.com"));
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        String requestBody = """
                {
                  "fullName": "David Updated",
                  "phone": "+255713456789",
                  "email": "updated@example.com"
                }
                """;

        mockMvc.perform(
                        put("/api/customers/{id}", customer.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId()))
                .andExpect(jsonPath("$.fullName").value("David Updated"))
                .andExpect(jsonPath("$.phone").value("0713456789"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.totalOrders").value(0))
                .andExpect(jsonPath("$.totalSpent").value(0))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldReturnBadRequestForInvalidRequest() throws Exception {
        String requestBody = """
                {
                  "fullName": "",
                  "phone": "123456789",
                  "email": "invalid-email"
                }
                """;

        mockMvc.perform(
                        post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.path").value("/api/customers"))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.fullName").exists())
                .andExpect(jsonPath("$.fieldErrors.phone").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void shouldReturnConflictForDuplicatePhone() throws Exception {
        String requestBody = """
                {
                  "fullName": "Another Customer",
                  "phone": "0712345678",
                  "email": "another@example.com"
                }
                """;

        mockMvc.perform(
                        post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Customer with phone '0712345678' already exists"))
                .andExpect(jsonPath("$.path").value("/api/customers"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnConflictForDuplicateEmail() throws Exception {
        String requestBody = """
                {
                  "fullName": "Another Customer",
                  "phone": "0712345679",
                  "email": "DAVID@EXAMPLE.COM"
                }
                """;

        mockMvc.perform(
                        post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Customer with email 'david@example.com' already exists"))
                .andExpect(jsonPath("$.path").value("/api/customers"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnNotFoundForMissingCustomer() throws Exception {
        mockMvc.perform(
                        get("/api/customers/{id}", 999999L)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Customer with ID '999999' not found"))
                .andExpect(jsonPath("$.path").value("/api/customers/999999"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnNotFoundForMissingPhone() throws Exception {
        mockMvc.perform(
                        get("/api/customers/phone/{phone}",
                                "0712345679")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Customer with phone '0712345679' not found"))
                .andExpect(jsonPath("$.path")
                        .value("/api/customers/phone/0712345679"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnNotFoundForMissingEmail() throws Exception {
        mockMvc.perform(
                        get("/api/customers/email/{email}",
                                "missing@example.com")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Customer with email 'missing@example.com' not found"))
                .andExpect(jsonPath("$.path")
                        .value("/api/customers/email/missing@example.com"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnConflictWhenUpdatingToExistingPhone() throws Exception {
        Customer secondCustomer = createCustomer(
                "Second Customer",
                "0712345679",
                "second@example.com"
        );

        String requestBody = """
                {
                  "fullName": "Second Customer Updated",
                  "phone": "0712345678",
                  "email": "second@example.com"
                }
                """;

        mockMvc.perform(
                        put("/api/customers/{id}", secondCustomer.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Customer with phone '0712345678' already exists"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnConflictWhenUpdatingToExistingEmail() throws Exception {
        Customer secondCustomer = createCustomer(
                "Second Customer",
                "0712345679",
                "second@example.com"
        );

        String requestBody = """
                {
                  "fullName": "Second Customer Updated",
                  "phone": "0712345679",
                  "email": "david@example.com"
                }
                """;

        mockMvc.perform(
                        put("/api/customers/{id}", secondCustomer.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Customer with email 'david@example.com' already exists"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    private Customer createCustomer(
            String fullName,
            String phone,
            String email
    ) {
        Customer customer = new Customer();

        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setTotalOrders(0);
        customer.setTotalSpent(BigDecimal.ZERO);

        OffsetDateTime now = OffsetDateTime.now();

        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);

        return customerRepository.save(customer);
    }
}