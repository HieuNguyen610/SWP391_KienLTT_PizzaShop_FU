package com.swp.pizzashop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${pizzashop.uploads.dir:#{systemProperties['user.dir'] + '/uploads'}}")
    private String uploadsRoot;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = uploadsRoot.replace("\\", "/");
        if (!path.endsWith("/")) path = path + "/";
        // Backward compatibility: serve both /uploads/** and /images/upload/** from the same physical directory
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + path);
        registry.addResourceHandler("/images/upload/**")
                .addResourceLocations("file:" + path);
    }
}
