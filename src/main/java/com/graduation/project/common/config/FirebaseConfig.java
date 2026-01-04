package com.graduation.project.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class FirebaseConfig {

  @Value("${app.firebase.config-path}")
  private String configPath;

  @Value("${app.firebase.bucket-name}")
  private String bucketName;

  private final ResourceLoader resourceLoader;

  public FirebaseConfig(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @PostConstruct
  public void init() {
    try {
      if (FirebaseApp.getApps().isEmpty()) {
        Resource resource = resourceLoader.getResource(configPath);
        try (InputStream serviceAccount = resource.getInputStream()) {
          FirebaseOptions options = FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccount))
              .setStorageBucket(bucketName)
              .build();

          FirebaseApp.initializeApp(options);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to initialize Firebase", e);
    }
  }
}
