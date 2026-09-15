package com.david.rebbystorebackend.security;

import com.david.rebbystorebackend.security.jwt.JwtService;
import com.david.rebbystorebackend.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityFilterChainTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void shouldReachTestEndpointWithoutToken() throws Exception {
        mockMvc.perform(
                        get("/test/protected")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("anonymous"));
    }

    @Test
    void shouldAuthenticateRequestWithValidJwt() throws Exception {
        UserPrincipal principal = UserPrincipal.createForTesting(
                1L,
                "admin",
                "admin@rebbystore.co.tz",
                "$2a$10$dummy",
                true,
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        );

        when(jwtService.isTokenValid("valid-jwt"))
                .thenReturn(true);

        when(jwtService.extractUsername("valid-jwt"))
                .thenReturn("admin");

        when(userDetailsService.loadUserByUsername("admin"))
                .thenReturn(principal);

        mockMvc.perform(
                        get("/test/protected")
                                .header(
                                        "Authorization",
                                        "Bearer valid-jwt"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(content().string("admin"));
    }

    @Test
    void shouldNotAuthenticateRequestWithInvalidJwt() throws Exception {
        when(jwtService.isTokenValid("invalid-jwt"))
                .thenReturn(false);

        mockMvc.perform(
                        get("/test/protected")
                                .header(
                                        "Authorization",
                                        "Bearer invalid-jwt"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(content().string("anonymous"));
    }
}
