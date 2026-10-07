package com.shehia_management.api.shared.security;

import com.shehia_management.api.identity.User;
import com.shehia_management.api.identity.UserStatus;
import com.shehia_management.api.identity.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String zanId) throws UsernameNotFoundException {
        User user = userRepository.findByZanId(zanId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with zanId: " + zanId));

        // Convert your User entity into Spring Security UserDetails
        boolean enabled = user.getStatus() == UserStatus.ACTIVE;
        return new org.springframework.security.core.userdetails.User(
                user.getZanId(),
                user.getPassword(),
                enabled,
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
}}