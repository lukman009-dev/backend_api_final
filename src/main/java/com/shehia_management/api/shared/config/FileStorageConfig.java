package com.shehia_management.api.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/docs/generated/**")
                .addResourceLocations("file:uploads/letters/");

        // NEW: serves everything FileStorageServiceImpl saves — resident ID
        // documents, proof of residence, letter supporting docs, and issue
        // photos — so the existing "Open document" / "View attached photo"
        // links in the admin app keep working once those *Url fields point
        // here instead of an externally-typed URL.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
