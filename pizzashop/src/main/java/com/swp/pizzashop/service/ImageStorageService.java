package com.swp.pizzashop.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    // Stores a food image and returns a public URL path (e.g., /uploads/foods/uuid.jpg)
    String storeFoodImage(MultipartFile file);

    // Optionally remove a previously stored file; returns true if deleted
    boolean deleteByPublicPath(String publicPath);
}

