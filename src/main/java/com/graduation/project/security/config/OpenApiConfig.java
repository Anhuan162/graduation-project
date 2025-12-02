package com.graduation.project.security.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "Graduation Project - Authentication API",
            version = "1.0.0",
            description = "Các API phục vụ xác thực và quản lý phiên người dùng.",
            contact = @Contact(name = "Graduation Project Team", email = "support@graduation.com")),
    servers = {
      @Server(url = "http://localhost:8080", description = "Local Server"),
    })
public class OpenApiConfig {}
