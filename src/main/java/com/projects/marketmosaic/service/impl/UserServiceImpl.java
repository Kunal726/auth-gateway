package com.projects.marketmosaic.service.impl;

import com.projects.marketmosaic.common.dto.resp.BaseRespDTO;
import com.projects.marketmosaic.dtos.ShippingAddressDTO;
import com.projects.marketmosaic.dtos.UpdateUserDTO;
import com.projects.marketmosaic.dtos.UserDTO;
import com.projects.marketmosaic.entity.ShippingAddressEntity;
import com.projects.marketmosaic.entity.UserEntity;
import com.projects.marketmosaic.exception.exceptions.AuthException;
import com.projects.marketmosaic.repositories.UserRepository;
import com.projects.marketmosaic.service.TokenBlackListService;
import com.projects.marketmosaic.service.UserService;
import com.projects.marketmosaic.utils.FileUtils;
import com.projects.marketmosaic.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final TokenBlackListService tokenBlackListService;
    private final FileUtils fileUtils;
    private final SecurityUtils securityUtils;

    @Override
    public BaseRespDTO getUser(String username, HttpServletRequest request) {
        securityUtils.validateUser(username, request);

        // Check if user exists
        UserEntity user = securityUtils.getUserByUsername(username);

        // Convert to DTO
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setProfilePicture(user.getProfilePicture());
        userDTO.setLastLogin(user.getLastLogin());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());

        // Get active shipping addresses
        List<ShippingAddressEntity> activeAddresses = user.getShippingAddresses().stream()
                .filter(ShippingAddressEntity::isActive)
                .toList();
        userDTO.setShippingAddresses(activeAddresses.stream()
                .map(this::convertAddressToDTO)
                .toList());

        // Set seller information if applicable
        if (user.getSeller() != null) {
            userDTO.setSeller(true);
            userDTO.setBusinessName(user.getSeller().getBusinessName());
            userDTO.setBusinessAddress(user.getSeller().getAddress());
            userDTO.setContactPhone(user.getSeller().getContactPhone());
            userDTO.setSellerStatus(user.getSeller().getSellerStatus().name());
        } else {
            userDTO.setSeller(false);
        }

        BaseRespDTO respDTO = new BaseRespDTO();
        respDTO.setStatus(true);
        respDTO.setMessage("User data retrieved successfully");
        respDTO.setCode("200");
        respDTO.setData(userDTO);

        return respDTO;
    }

    @Override
    @Transactional
    public BaseRespDTO updateUser(String username, UpdateUserDTO updateUserDTO, String profilePicturePath,
                                  HttpServletRequest request) {
        securityUtils.validateUser(username, request);

        // Check if user exists
        UserEntity user = securityUtils.getUserByUsername(username);

        // Update user fields if they are provided in the DTO
        if (updateUserDTO != null) {
            if (updateUserDTO.getName() != null) {
                user.setName(updateUserDTO.getName());
            }
            if (updateUserDTO.getEmail() != null) {
                // Check if email is already in use by another user
                userRepository.findByEmail(updateUserDTO.getEmail())
                        .ifPresent(existingUser -> {
                            if (!existingUser.getId().equals(user.getId())) {
                                throw AuthException.emailInUse();
                            }
                        });
                user.setEmail(updateUserDTO.getEmail());
            }
            if (updateUserDTO.getPhoneNumber() != null) {
                user.setPhoneNumber(updateUserDTO.getPhoneNumber());
            }
        }

        // Update profile picture if provided
        if (profilePicturePath != null) {
            // Delete old profile picture if exists
            fileUtils.deleteProfilePicture(user.getProfilePicture());
            user.setProfilePicture(profilePicturePath);
        }

        // Save the updated user
        userRepository.save(user);

        return getUser(username, request);
    }

    @Override
    @Transactional
    public BaseRespDTO deleteUser(String username, HttpServletRequest request) {
        securityUtils.validateUser(username, request);

        // Check if user exists
        UserEntity user = securityUtils.getUserByUsername(username);

        // Invalidate all user tokens
        tokenBlackListService.invalidateAllUserTokens(username);

        // Delete the user
        userRepository.delete(user);

        BaseRespDTO respDTO = new BaseRespDTO();
        respDTO.setStatus(true);
        respDTO.setMessage("User deleted successfully");
        respDTO.setCode("200");

        return respDTO;
    }

    private ShippingAddressDTO convertAddressToDTO(ShippingAddressEntity entity) {
        ShippingAddressDTO dto = new ShippingAddressDTO();
        dto.setId(entity.getId());
        dto.setAddressLine1(entity.getAddressLine1());
        dto.setAddressLine2(entity.getAddressLine2());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setCountry(entity.getCountry());
        dto.setPostalCode(entity.getPostalCode());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setAddressType(entity.getAddressType());
        dto.setDefault(entity.isDefault());
        return dto;
    }
}