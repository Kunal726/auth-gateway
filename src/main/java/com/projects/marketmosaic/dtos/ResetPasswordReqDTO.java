package com.projects.marketmosaic.dtos;

import lombok.Data;

@Data
public class ResetPasswordReqDTO {
    private String token;
    private String newPassword;
}