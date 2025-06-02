package com.projects.marketmosaic.service;

import com.projects.marketmosaic.common.dto.resp.BaseRespDTO;
import com.projects.marketmosaic.common.dto.resp.TokenValidationRespDTO;
import com.projects.marketmosaic.dtos.LoginReqDTO;
import com.projects.marketmosaic.dtos.RegisterReqDTO;
import com.projects.marketmosaic.dtos.ForgotPasswordReqDTO;
import com.projects.marketmosaic.dtos.ResetPasswordReqDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    BaseRespDTO loginUser(LoginReqDTO loginReqDTO, HttpServletResponse response);

    BaseRespDTO registerUser(RegisterReqDTO reqDTO);

    BaseRespDTO logout(String authHeader, HttpServletRequest request, HttpServletResponse response);

    BaseRespDTO logoutAllSessions(String authHeader, HttpServletRequest request, HttpServletResponse response);

    TokenValidationRespDTO validateToken(String token);

    BaseRespDTO forgotPassword(ForgotPasswordReqDTO reqDTO);

    BaseRespDTO resetPassword(ResetPasswordReqDTO reqDTO);

    TokenValidationRespDTO validateUserToken();
}
