package com.projects.marketmosaic.service.impl;

import com.projects.marketmosaic.constants.ErrorMessages;
import com.projects.marketmosaic.common.dto.resp.BaseRespDTO;
import com.projects.marketmosaic.entity.SellerEntity;
import com.projects.marketmosaic.entity.UserEntity;
import com.projects.marketmosaic.enums.AuthStatus;
import com.projects.marketmosaic.enums.SellerStatus;
import com.projects.marketmosaic.exception.exceptions.AuthException;
import com.projects.marketmosaic.repositories.SellerRepository;
import com.projects.marketmosaic.repositories.UserRepository;
import com.projects.marketmosaic.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;

    @Override
    @Transactional
    public BaseRespDTO approveSeller(String username) {
        BaseRespDTO respDTO = new BaseRespDTO();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(AuthException::userNotFound);

        if (user.getSeller().getSellerStatus() != SellerStatus.PENDING) {
            throw new AuthException("User is not pending seller approval", AuthStatus.AUTH_006, HttpStatus.BAD_REQUEST);
        }

        user.getSeller().setSellerStatus(SellerStatus.APPROVED);
        if (!user.getRole().contains("SELLER")) {
            user.setRole("SELLER");
        }
        userRepository.save(user);

        respDTO.setStatus(true);
        respDTO.setCode("200");
        respDTO.setMessage("Seller status approved successfully");
        return respDTO;
    }

    @Override
    @Transactional
    public BaseRespDTO rejectSeller(String username) {
        BaseRespDTO respDTO = new BaseRespDTO();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(AuthException::userNotFound);

        if (user.getSeller().getSellerStatus() != SellerStatus.PENDING) {
            throw new AuthException("User is not pending seller approval", AuthStatus.AUTH_006, HttpStatus.BAD_REQUEST);
        }

        user.getSeller().setSellerStatus(SellerStatus.REJECTED);
        userRepository.save(user);

        respDTO.setStatus(true);
        respDTO.setCode("200");
        respDTO.setMessage(ErrorMessages.SELLER_STATUS_REJECTED);
        return respDTO;
    }

    @Override
    public BaseRespDTO getPendingSellers() {
        List<SellerEntity> pendingSellers = sellerRepository.findBySellerStatus(SellerStatus.PENDING);

        BaseRespDTO respDTO = new BaseRespDTO();
        respDTO.setStatus(true);
        respDTO.setCode("200");
        respDTO.setMessage("Pending sellers retrieved successfully");
        respDTO.setData(pendingSellers);
        return respDTO;
    }

    @Override
    @Transactional
    public BaseRespDTO revokeSeller(String username) {
        BaseRespDTO respDTO = new BaseRespDTO();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(AuthException::userNotFound);

        if (user.getSeller() == null || user.getSeller().getSellerStatus() != SellerStatus.APPROVED) {
            throw new AuthException("User is not an approved seller", AuthStatus.AUTH_006, HttpStatus.BAD_REQUEST);
        }

        user.getSeller().setSellerStatus(SellerStatus.REVOKED);
        userRepository.save(user);

        respDTO.setStatus(true);
        respDTO.setCode("200");
        respDTO.setMessage("Seller status revoked");
        return respDTO;
    }
}