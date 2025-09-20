package com.form.form_back.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configuration pour servir les fichiers uploadés
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/uploads/");

        registry.addResourceHandler("/signatures/**")
                .addResourceLocations("file:" + uploadDir + "/signatures/");

        registry.addResourceHandler("/drawings/**")
                .addResourceLocations("file:" + uploadDir + "/drawings/");

        registry.addResourceHandler("/audio/**")
                .addResourceLocations("file:" + uploadDir + "/audio/");
    }
}