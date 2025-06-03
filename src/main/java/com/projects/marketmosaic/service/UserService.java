package com.projects.marketmosaic.service;

import com.projects.marketmosaic.common.dto.resp.BaseRespDTO;
import com.projects.marketmosaic.dtos.RegisterReqDTO;
import com.projects.marketmosaic.dtos.UpdateUserDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {
    BaseRespDTO getUser(String username, HttpServletRequest request);

    BaseRespDTO updateUser(String username, UpdateUserDTO updateUserDTO, String profilePicturePath,
            HttpServletRequest request);

    BaseRespDTO deleteUser(String username, HttpServletRequest request);

    BaseRespDTO addAdmin(RegisterReqDTO registerReqDTO);
}