package com.smartcampus.util;

import com.smartcampus.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.smartcampus.config.FileStorageConfig;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Utility class to securely save, load, and validate image attachments to the local file system.
 */
@Component
public class FileStorageUtil {

    private final Path fileStorageLocation;
    
    // Allowed MIME types explicitly set based on Workflow Rules
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    @Autowired
    public FileStorageUtil(FileStorageConfig fileStorageConfig) {
        this.fileStorageLocation = Paths.get(fileStorageConfig.getUploadDir())
                .toAbsolutePath().normalize();
    }

    /**
     * Stores the file and returns the generated UUID filename.
     */
    public String storeFile(MultipartFile file) {
        validateFile(file);

        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        
        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        // Generate safe unique filename to avoid path traversal / overwriting
        String newFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            if (newFileName.contains("..")) {
                throw new FileUploadException("Configuration issue: generated filename contains invalid path sequence " + newFileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return newFileName;
        } catch (IOException ex) {
            throw new FileUploadException("Could not store file " + originalFileName + ". Please try again!");
        }
    }

    /**
     * Loads a file from the disk as a Resource for downloading.
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileUploadException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileUploadException("File not found " + fileName);
        }
    }

    /**
     * Delete file from the disk
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileUploadException("Could not delete file " + fileName);
        }
    }

    /**
     * Validate against empty files and invalid MIME types.
     * The Maximum Size validation is handled globally by Spring Servlet properties, but we assert it here defensively.
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileUploadException("Failed to store empty file.");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new FileUploadException("Invalid file type. Only JPEG, PNG, GIF, and WEBP images are allowed.");
        }
    }
}
