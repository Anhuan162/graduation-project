package com.graduation.project.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data // Lombok để tự tạo Getter/Setter
@Component
@ConfigurationProperties(prefix = "google.drive") // Key bắt đầu bằng "google.drive"
public class GoogleDriveProperties {
  private String clientId;
  private String clientSecret;
  private String refreshToken;
}
