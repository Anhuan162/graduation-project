package com.graduation.project.common.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

  private final GoogleDriveProperties driveProperties;

  public FirebaseConfig(GoogleDriveProperties driveProperties) {
    this.driveProperties = driveProperties;
  }

  @Bean
  public FirebaseApp initialFirebaseApp() throws IOException {
    String serviceAccountPath =
        System.getProperty("user.dir") + "/graduated-project-firebase-adminsdk-key.json";
    FileInputStream serviceAccountStream = new FileInputStream(serviceAccountPath);

    if (FirebaseApp.getApps().isEmpty()) {
      FirebaseOptions options =
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
              .setStorageBucket("graduated-project-17647.firebasestorage.app")
              .build();
      return FirebaseApp.initializeApp(options);
    }
    return FirebaseApp.getInstance();
  }

  @Bean
  public Drive googleDrive() throws IOException, GeneralSecurityException {
    // Lấy giá trị từ file YAML thông qua class Properties
    String clientId = driveProperties.getClientId();
    String clientSecret = driveProperties.getClientSecret();
    String refreshToken = driveProperties.getRefreshToken();

    // Check log để test (xóa khi chạy thật)
    System.out.println("Client ID đang dùng: " + clientId);
      System.out.println("DEBUG Client ID: " + clientId);
      System.out.println("DEBUG Refresh Token: " + refreshToken);
    UserCredentials credentials =
        UserCredentials.newBuilder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setRefreshToken(refreshToken)
            .build();

    return new Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            new HttpCredentialsAdapter(credentials))
        .setApplicationName("Graduation-Project")
        .build();
  }
}
