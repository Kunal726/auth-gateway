package com.projects.marketmosaic.service.impl;

import com.projects.marketmosaic.constants.ErrorMessages;
import com.projects.marketmosaic.common.dto.resp.BaseRespDTO;
import com.projects.marketmosaic.dtos.ShippingAddressDTO;
import com.projects.marketmosaic.entity.ShippingAddressEntity;
import com.projects.marketmosaic.entity.UserEntity;
import com.projects.marketmosaic.enums.AuthStatus;
import com.projects.marketmosaic.exception.exceptions.AuthException;
import com.projects.marketmosaic.repositories.ShippingAddressRepository;
import com.projects.marketmosaic.service.ShippingAddressService;
import com.projects.marketmosaic.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShippingAddressServiceImpl implements ShippingAddressService {

    private final ShippingAddressRepository addressRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public void addAddress(String username, ShippingAddressDTO addressDTO, HttpServletRequest request) {
        securityUtils.validateUser(username, request);

        UserEntity user = securityUtils.getUserByUsername(username);
        ShippingAddressEntity address = new ShippingAddressEntity();
        updateAddressFields(address, addressDTO);
        address.setUser(user);

        // If this is the first address, make it default
        if (user.getShippingAddresses().isEmpty()) {
            address.setDefault(true);
        } else if (addressDTO.isDefault()) {
            // If new address is default, remove default from others
            removeDefaultFromOtherAddresses(user.getId());
        }

        addressRepository.save(address);
    }

    @Override
    @Transactional
    public void updateAddress(String username, Long addressId, ShippingAddressDTO addressDTO,
                              HttpServletRequest request) {
        securityUtils.validateUser(username, request);

        UserEntity user = securityUtils.getUserByUsername(username);
        ShippingAddressEntity address = getAddressById(addressId, user.getId());

        updateAddressFields(address, addressDTO);

        if (addressDTO.isDefault()) {
            removeDefaultFromOtherAddresses(user.getId());
            address.setDefault(true);
        }

        addressRepository.save(address);
    }

    @Override
    @Transactional
    public BaseRespDTO deleteAddress(String username, Long addressId, HttpServletRequest request) {
        securityUtils.validateUser(username, request);

        UserEntity user = securityUtils.getUserByUsername(username);
        ShippingAddressEntity address = getAddressById(addressId, user.getId());

        // If deleting default address, make another address default if exists
        if (address.isDefault()) {
            List<ShippingAddressEntity> otherAddresses = user.getShippingAddresses().stream()
                    .filter(a -> !a.getId().equals(addressId) && a.isActive())
                    .toList();
            if (!otherAddresses.isEmpty()) {
                otherAddresses.get(0).setDefault(true);
                addressRepository.save(otherAddresses.get(0));
            }
        }

        // Soft delete
        address.setActive(false);
        addressRepository.save(address);

        BaseRespDTO respDTO = new BaseRespDTO();
        respDTO.setStatus(true);
        respDTO.setMessage("Shipping address deleted successfully");
        respDTO.setCode("200");
        return respDTO;
    }

    @Override
    public BaseRespDTO getAddresses(String username, HttpServletRequest request) {
        securityUtils.validateUser(username, request);

        UserEntity user = securityUtils.getUserByUsername(username);
        List<ShippingAddressEntity> addresses = addressRepository.findByUserIdAndIsActiveTrue(user.getId());

        BaseRespDTO respDTO = new BaseRespDTO();
        respDTO.setStatus(true);
        respDTO.setMessage("Shipping addresses retrieved successfully");
        respDTO.setCode("200");
        respDTO.setData(addresses.stream().map(this::convertToDTO).collect(Collectors.toList()));
        return respDTO;
    }

    @Override
    @Transactional
    public BaseRespDTO setDefaultAddress(String username, Long addressId, HttpServletRequest request) {
        securityUtils.validateUser(username, request);

        UserEntity user = securityUtils.getUserByUsername(username);
        ShippingAddressEntity address = getAddressById(addressId, user.getId());

        removeDefaultFromOtherAddresses(user.getId());
        address.setDefault(true);
        addressRepository.save(address);

        BaseRespDTO respDTO = new BaseRespDTO();
        respDTO.setStatus(true);
        respDTO.setMessage("Default shipping address updated successfully");
        respDTO.setCode("200");
        respDTO.setData(convertToDTO(address));
        return respDTO;
    }

    private ShippingAddressEntity getAddressById(Long addressId, Long userId) {
        return addressRepository.findByIdAndUserIdAndIsActiveTrue(addressId, userId)
                .orElseThrow(() -> new AuthException(ErrorMessages.ADDRESS_NOT_FOUND, AuthStatus.AUTH_005,
                        HttpStatus.NOT_FOUND));
    }

    private void removeDefaultFromOtherAddresses(Long userId) {
        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(address -> {
                    address.setDefault(false);
                    addressRepository.save(address);
                });
    }

    private void updateAddressFields(ShippingAddressEntity address, ShippingAddressDTO dto) {
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setCountry(dto.getCountry());
        address.setPostalCode(dto.getPostalCode());
        address.setPhoneNumber(dto.getPhoneNumber());
        address.setAddressType(dto.getAddressType());
    }

    private ShippingAddressDTO convertToDTO(ShippingAddressEntity entity) {
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