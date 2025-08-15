package com.spring.springbootapplication.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${app.upload-dir:uploads}")   // ← application.properties で変更可
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String location = "file:" + Paths.get(uploadDir)
            .toAbsolutePath()
            .normalize()
            .toString()
                      + "/";            // 末尾スラッシュ必須
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations(location);
    }
}
