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
        String pathNew = uploadsRoot.replace("\\", "/");
        if (!pathNew.endsWith("/")) pathNew = pathNew + "/";
        // Backward compatibility: serve both new and legacy directories
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + pathNew);
        registry.addResourceHandler("/images/upload/**")
                .addResourceLocations("file:" + pathNew);
        // Some records may have stored /upload/** (singular) paths
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + pathNew);
    }
}
