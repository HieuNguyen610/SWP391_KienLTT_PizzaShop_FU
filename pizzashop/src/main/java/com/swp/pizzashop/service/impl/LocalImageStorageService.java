package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.service.ImageStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class LocalImageStorageService implements ImageStorageService {

    @Value("${pizzashop.uploads.dir:#{systemProperties['user.dir'] + '/uploads'}}")
    private String uploadsRoot;

    @Value("${pizzashop.uploads.max-size-bytes:5242880}")
    private long maxSizeBytes; // default 5MB

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @Override
    public String storeFoodImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No image file provided");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("Image too large. Max " + maxSizeBytes + " bytes");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported image type: " + contentType);
        }
        String ext = resolveExtension(file);
        String filename = UUID.randomUUID() + (ext.isEmpty() ? "" : ("." + ext));
        Path dir = Path.of(uploadsRoot, "foods");
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(filename).normalize();
            // Prevent path traversal
            if (!target.startsWith(dir)) {
                throw new SecurityException("Invalid path");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored food image at {}", target);
            return "/uploads/foods/" + filename;
        } catch (IOException ex) {
            log.error("Failed to store image", ex);
            throw new RuntimeException("Failed to store image");
        }
    }

    @Override
    public boolean deleteByPublicPath(String publicPath) {
        if (publicPath == null || publicPath.isBlank()) return false;
        String prefix = "/uploads/";
        if (!publicPath.startsWith(prefix)) return false; // only allow deleting in our uploads mapping
        String relative = publicPath.substring(prefix.length());
        Path target = Path.of(uploadsRoot).resolve(relative).normalize();
        try {
            if (Files.exists(target)) {
                Files.delete(target);
                log.info("Deleted stored image {}", target);
                return true;
            }
        } catch (IOException ex) {
            log.warn("Failed to delete stored image {}: {}", target, ex.getMessage());
        }
        return false;
    }

    private String resolveExtension(MultipartFile file) {
        String original = file.getOriginalFilename();
        String ext = "";
        if (StringUtils.hasText(original)) {
            int dot = original.lastIndexOf('.');
            if (dot >= 0 && dot < original.length() - 1) {
                ext = original.substring(dot + 1).toLowerCase();
            }
        }
        if (ext.isBlank()) {
            String ct = file.getContentType();
            if (ct != null) {
                switch (ct.toLowerCase()) {
                    case "image/jpeg": return "jpg";
                    case "image/png": return "png";
                    case "image/gif": return "gif";
                    case "image/webp": return "webp";
                    default: return "";
                }
            }
        }
        // sanitize common double extensions
        return ext.replaceAll("[^a-z0-9]", "");
    }
}

