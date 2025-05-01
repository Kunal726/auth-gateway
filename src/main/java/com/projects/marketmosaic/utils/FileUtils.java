package com.projects.marketmosaic.utils;

import com.projects.marketmosaic.enums.AuthStatus;
import com.projects.marketmosaic.exception.exceptions.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

@Component
@Slf4j
public class FileUtils {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L; // 5MB

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String saveProfilePicture(MultipartFile file, String username) {
        validateFile(file);

        try {
            // Create uploads directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Get file extension
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";

            // Generate unique filename
            String filename = username + "_" + UUID.randomUUID() + fileExtension;

            // Create the complete path
            Path filePath = uploadPath.resolve(filename);

            // Save the file
            Files.copy(file.getInputStream(), filePath);

            // Return the relative path to be stored in DB
            return filename;

        } catch (IOException e) {
            throw new AuthException("Failed to save profile picture",
                    AuthStatus.AUTH_007, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new AuthException("File is empty",
                    AuthStatus.AUTH_007, HttpStatus.BAD_REQUEST);
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AuthException("File size exceeds maximum limit of 5MB",
                    AuthStatus.AUTH_007, HttpStatus.BAD_REQUEST);
        }

        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp").contains(extension)) {
                throw new AuthException("Invalid file extension. Only jpg, jpeg, png, gif, bmp, and webp are allowed",
                        AuthStatus.AUTH_007, HttpStatus.BAD_REQUEST);
            }
        }
    }

    public void deleteProfilePicture(String filename) {
        if (filename != null) {
            try {
                Path filePath = Paths.get(uploadDir).resolve(filename);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log error but don't throw exception as this is not critical
                log.error("Failed to delete profile picture: {}", e.getMessage());
            }
        }
    }
}