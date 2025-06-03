package com.projects.marketmosaic.controller;

import com.projects.marketmosaic.common.dto.resp.BaseRespDTO;
import com.projects.marketmosaic.dtos.RegisterReqDTO;
import com.projects.marketmosaic.dtos.UpdateUserDTO;
import com.projects.marketmosaic.service.UserService;
import com.projects.marketmosaic.utils.FileUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final FileUtils fileUtils;

    @GetMapping("/{username}")
    public ResponseEntity<BaseRespDTO> getUser(
            @PathVariable String username,
            HttpServletRequest request) {
        return ResponseEntity.ok(userService.getUser(username, request));
    }

    @PutMapping(value = "/{username}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseRespDTO> updateUser(
            @PathVariable String username,
            @RequestPart(required = false) UpdateUserDTO updateUserDTO,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            HttpServletRequest request) {

        // Handle profile picture upload if provided
        String profilePicturePath = null;
        if (profilePicture != null && !profilePicture.isEmpty()) {
            profilePicturePath = fileUtils.saveProfilePicture(profilePicture, username);
        }

        return ResponseEntity.ok(userService.updateUser(username, updateUserDTO, profilePicturePath, request));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<BaseRespDTO> deleteUser(
            @PathVariable String username,
            HttpServletRequest request) {
        return ResponseEntity.ok(userService.deleteUser(username, request));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(value = "/admin/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseRespDTO> createAdmin(@RequestBody RegisterReqDTO registerReqDTO) {
        return ResponseEntity.ok(userService.addAdmin(registerReqDTO));
    }
}