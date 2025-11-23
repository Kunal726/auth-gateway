package com.projects.marketmosaic.controller;

import com.projects.marketmosaic.common.dto.resp.BaseRespDTO;
import com.projects.marketmosaic.common.dto.user.ShippingAddressDTO;
import com.projects.marketmosaic.service.ShippingAddressService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{username}/addresses")
@RequiredArgsConstructor
public class ShippingAddressController {
    private final ShippingAddressService shippingAddressService;

    @GetMapping
    public ResponseEntity<BaseRespDTO> getAddresses(
            @PathVariable String username,
            HttpServletRequest request) {
        return ResponseEntity.ok(shippingAddressService.getAddresses(username, request));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<BaseRespDTO> getAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId,
            HttpServletRequest request) {
        return ResponseEntity.ok(shippingAddressService.getAddress(userId, addressId, request));
    }

    @PostMapping
    public ResponseEntity<Void> addAddress(
            @PathVariable String username,
            @RequestBody ShippingAddressDTO addressDTO,
            HttpServletRequest request) {
        shippingAddressService.addAddress(username, addressDTO, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<Void> updateAddress(
            @PathVariable String username,
            @PathVariable Long addressId,
            @RequestBody ShippingAddressDTO addressDTO,
            HttpServletRequest request) {
        shippingAddressService.updateAddress(username, addressId, addressDTO, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<BaseRespDTO> deleteAddress(
            @PathVariable String username,
            @PathVariable Long addressId,
            HttpServletRequest request) {
        return ResponseEntity.ok(shippingAddressService.deleteAddress(username, addressId, request));
    }

    @PutMapping("/{addressId}/default")
    public ResponseEntity<BaseRespDTO> setDefaultAddress(
            @PathVariable String username,
            @PathVariable Long addressId,
            HttpServletRequest request) {
        return ResponseEntity.ok(shippingAddressService.setDefaultAddress(username, addressId, request));
    }
}