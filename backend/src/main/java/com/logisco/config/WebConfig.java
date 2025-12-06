package com.logisco.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String workingDir = System.getProperty("user.dir");
        Path frontendPath;

        if (workingDir.endsWith("backend")) {
            frontendPath = Paths.get(workingDir).getParent().resolve("frontend").toAbsolutePath().normalize();
        } else {
            frontendPath = Paths.get(workingDir, "frontend").toAbsolutePath().normalize();
        }

        registry.addResourceHandler("/*.html")
                .addResourceLocations("file:" + frontendPath + "/");

        registry.addResourceHandler("/css/**")
                .addResourceLocations("file:" + frontendPath + "/css/");

        registry.addResourceHandler("/js/**")
                .addResourceLocations("file:" + frontendPath + "/js/");

        registry.addResourceHandler("/admin/**")
                .addResourceLocations("file:" + frontendPath + "/admin/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}

