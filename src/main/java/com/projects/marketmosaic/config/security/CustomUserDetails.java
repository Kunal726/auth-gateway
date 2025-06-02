package com.projects.marketmosaic.config.security;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Builder
public class CustomUserDetails implements UserDetails {

    @Setter
    @Getter
    private Long userId;

    @Getter
    @Setter
    private String email;

    @Setter
    private String username;

    @Setter
    private String password;

    @Setter
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }
}
