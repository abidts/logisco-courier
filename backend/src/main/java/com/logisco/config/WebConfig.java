package com.logisco.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve frontend files from the frontend directory (relative to backend folder)
        Path backendPath = Paths.get(System.getProperty("user.dir"));
        Path frontendPath = backendPath.getParent().resolve("frontend").toAbsolutePath().normalize();
        
        registry.addResourceHandler("/*.html")
                .addResourceLocations("file:" + frontendPath + "/");
        
        registry.addResourceHandler("/css/**")
                .addResourceLocations("file:" + frontendPath + "/css/");
        
        registry.addResourceHandler("/js/**")
                .addResourceLocations("file:" + frontendPath + "/js/");
        
        registry.addResourceHandler("/admin/**")
                .addResourceLocations("file:" + frontendPath + "/admin/");
    }
}

