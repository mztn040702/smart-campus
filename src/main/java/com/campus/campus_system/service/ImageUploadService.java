package com.campus.campus_system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageUploadService {
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path imageUploadDir;

    public ImageUploadService(@Value("${app.upload.image-dir:uploads/images}") String imageUploadDir) {
        this.imageUploadDir = Path.of(imageUploadDir).toAbsolutePath().normalize();
    }

    public String uploadImage(MultipartFile file) {
        validateImage(file);

        try {
            Files.createDirectories(imageUploadDir);
            String extension = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + "." + extension;
            Path target = imageUploadDir.resolve(filename);
            file.transferTo(target);
            return "/uploads/images/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Image file is required");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new RuntimeException("Image size must be 5MB or less");
        }

        String extension = getExtension(file.getOriginalFilename());
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension) || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }
    }

    private String getExtension(String filename) {
        if (filename == null) {
            return "";
        }

        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDot + 1).toLowerCase();
    }
}
