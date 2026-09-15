package com.david.rebbystorebackend.security.service;

import com.david.rebbystorebackend.domain.entity.User;
import com.david.rebbystorebackend.repository.UserRepository;
import com.david.rebbystorebackend.security.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier)
            throws UsernameNotFoundException {

        if (identifier == null || identifier.trim().isEmpty()) {
            throw new UsernameNotFoundException("Username or email is required");
        }

        String normalizedIdentifier = identifier.trim();

        User user = userRepository.findByUsername(normalizedIdentifier)
                .orElseGet(() ->
                        userRepository.findByEmail(normalizedIdentifier.toLowerCase())
                                .orElseThrow(() ->
                                        new UsernameNotFoundException(
                                                "User not found"
                                        )
                                )
                );

        return UserPrincipal.from(user);
    }
}