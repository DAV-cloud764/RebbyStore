package com.david.rebbystorebackend.security;

import com.david.rebbystorebackend.domain.entity.Role;
import com.david.rebbystorebackend.domain.entity.User;
import com.david.rebbystorebackend.repository.UserRepository;
import com.david.rebbystorebackend.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void shouldLoadUserByUsername() {

        String rawPassword = "RebbyStore@123";
        String passwordHash = passwordEncoder.encode(rawPassword);

        UserRepository repository = mock(UserRepository.class);

        User user = createUser(
                "admin",
                "admin@rebbystore.co.tz",
                passwordHash,
                true,
                "ADMIN"
        );

        when(repository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        CustomUserDetailsService service =
                new CustomUserDetailsService(repository);

        UserPrincipal principal =
                (UserPrincipal) service.loadUserByUsername("admin");

        assertThat(principal.getUsername()).isEqualTo("admin");
        assertThat(principal.getEmail())
                .isEqualTo("admin@rebbystore.co.tz");

        assertThat(principal.getPassword())
                .isNotNull()
                .isNotBlank();

        assertThat(
                passwordEncoder.matches(
                        rawPassword,
                        principal.getPassword()
                )
        ).isTrue();

        assertThat(principal.getAuthorities())
                .extracting("authority")
                .contains("ROLE_ADMIN");
    }

    @Test
    void shouldLoadUserByEmail() {

        String rawPassword = "RebbyStore@123";
        String passwordHash = passwordEncoder.encode(rawPassword);

        UserRepository repository = mock(UserRepository.class);

        User user = createUser(
                "staff01",
                "staff@rebbystore.co.tz",
                passwordHash,
                true,
                "STAFF"
        );

        when(repository.findByUsername("staff@rebbystore.co.tz"))
                .thenReturn(Optional.empty());

        when(repository.findByEmail("staff@rebbystore.co.tz"))
                .thenReturn(Optional.of(user));

        CustomUserDetailsService service =
                new CustomUserDetailsService(repository);

        UserPrincipal principal =
                (UserPrincipal) service.loadUserByUsername(
                        "staff@rebbystore.co.tz"
                );

        assertThat(principal.getUsername())
                .isEqualTo("staff01");

        assertThat(principal.getEmail())
                .isEqualTo("staff@rebbystore.co.tz");

        assertThat(principal.getPassword())
                .isNotNull()
                .isNotBlank();

        assertThat(
                passwordEncoder.matches(
                        rawPassword,
                        principal.getPassword()
                )
        ).isTrue();

        assertThat(principal.getAuthorities())
                .extracting("authority")
                .contains("ROLE_STAFF");
    }

    @Test
    void shouldRejectUnknownUser() {

        UserRepository repository = mock(UserRepository.class);

        when(repository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        when(repository.findByEmail("unknown"))
                .thenReturn(Optional.empty());

        CustomUserDetailsService service =
                new CustomUserDetailsService(repository);

        assertThatThrownBy(
                () -> service.loadUserByUsername("unknown")
        )
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void shouldRejectBlankIdentifier() {

        UserRepository repository = mock(UserRepository.class);

        CustomUserDetailsService service =
                new CustomUserDetailsService(repository);

        assertThatThrownBy(
                () -> service.loadUserByUsername("   ")
        )
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Username or email is required");

        verifyNoInteractions(repository);
    }

    @Test
    void shouldExposeDisabledUserAsDisabled() {

        String rawPassword = "RebbyStore@123";
        String passwordHash = passwordEncoder.encode(rawPassword);

        UserRepository repository = mock(UserRepository.class);

        User user = createUser(
                "disabled",
                "disabled@rebbystore.co.tz",
                passwordHash,
                false,
                "STAFF"
        );

        when(repository.findByUsername("disabled"))
                .thenReturn(Optional.of(user));

        CustomUserDetailsService service =
                new CustomUserDetailsService(repository);

        UserPrincipal principal =
                (UserPrincipal) service.loadUserByUsername("disabled");

        assertThat(principal.isEnabled()).isFalse();

        assertThat(principal.getPassword())
                .isNotNull()
                .isNotBlank();

        assertThat(
                passwordEncoder.matches(
                        rawPassword,
                        principal.getPassword()
                )
        ).isTrue();
    }

    private User createUser(
            String username,
            String email,
            String passwordHash,
            boolean enabled,
            String roleName
    ) {

        Role role = new Role(roleName);

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setEnabled(enabled);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());

        user.addRole(role);

        return user;
    }
}