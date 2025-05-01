package com.projects.marketmosaic.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenValidationRespDTO {
    private boolean valid;
    private String username;
    private Long userId;
    private String email;
    private String name;
    private Collection<? extends GrantedAuthority> authorities;
}