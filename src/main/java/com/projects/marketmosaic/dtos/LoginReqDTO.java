package com.projects.marketmosaic.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginReqDTO {
    private String username;
    private String password;
}
