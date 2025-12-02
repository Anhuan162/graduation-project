package com.graduation.project.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.FileInputStream;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

  @Bean
  public FirebaseApp initialFirebaseApp() throws IOException {
    String serviceAccountPath = System.getProperty("user.dir") + "/graduated-project-firebase-adminsdk-key.json";
    FileInputStream serviceAccountStream = new FileInputStream(serviceAccountPath);

    if(FirebaseApp.getApps().isEmpty()) {
      FirebaseOptions options =
              FirebaseOptions.builder()
                      .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                      .setStorageBucket("graduated-project-17647.firebasestorage.app")
                      .build();
      return FirebaseApp.initializeApp(options);
    }
    return FirebaseApp.getInstance();
  }
}
