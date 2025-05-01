package com.projects.marketmosaic.controller;

import com.projects.marketmosaic.common.dto.resp.BaseRespDTO;
import com.projects.marketmosaic.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {
    private final SellerService sellerService;

    @PostMapping("/approve/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<BaseRespDTO> approveSeller(@PathVariable String username) {
        return ResponseEntity.ok(sellerService.approveSeller(username));
    }

    @PostMapping("/reject/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<BaseRespDTO> rejectSeller(@PathVariable String username) {
        return ResponseEntity.ok(sellerService.rejectSeller(username));
    }

    @PostMapping("/revoke/seller/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<BaseRespDTO> revokeSeller(@PathVariable String username) {
        return ResponseEntity.ok(sellerService.revokeSeller(username));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<BaseRespDTO> getPendingSellers() {
        return ResponseEntity.ok(sellerService.getPendingSellers());
    }
}