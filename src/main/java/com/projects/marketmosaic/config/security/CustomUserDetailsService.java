package com.projects.marketmosaic.config.security;

import com.projects.marketmosaic.entity.UserEntity;
import com.projects.marketmosaic.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
        private final UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                UserEntity user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found with username: " + username));

                List<SimpleGrantedAuthority> authorities = Arrays.stream(user.getRoles().split(","))
                                .map(String::trim)
                                .map(SimpleGrantedAuthority::new)
                                .toList();

                return User.builder()
                                .username(user.getUsername())
                                .password(user.getPassword())
                                .authorities(authorities)
                                .build();
        }
}