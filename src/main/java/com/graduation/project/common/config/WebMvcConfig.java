package com.graduation.project.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /uploads/** to the uploads directory in the project root
        // Using file: ensures it looks for an absolute filesystem path or relative to
        // working dir
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }

    @Override
    public void addArgumentResolvers(
            java.util.List<org.springframework.web.method.support.HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new org.springframework.data.web.PageableHandlerMethodArgumentResolver());
    }
}
