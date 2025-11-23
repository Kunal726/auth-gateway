package com.projects.marketmosaic.service;

import com.projects.marketmosaic.common.dto.resp.BaseRespDTO;
import com.projects.marketmosaic.common.dto.user.ShippingAddressDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface ShippingAddressService {
    void addAddress(String username, ShippingAddressDTO addressDTO, HttpServletRequest request);

    void updateAddress(String username, Long addressId, ShippingAddressDTO addressDTO,
            HttpServletRequest request);

    BaseRespDTO deleteAddress(String username, Long addressId, HttpServletRequest request);

    BaseRespDTO getAddresses(String username, HttpServletRequest request);

    BaseRespDTO setDefaultAddress(String username, Long addressId, HttpServletRequest request);

    BaseRespDTO getAddress(Long userId, Long addressId, HttpServletRequest request);
}