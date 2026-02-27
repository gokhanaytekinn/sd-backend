package com.sd.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Configuration
@Slf4j
public class FcmConfig {

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                org.springframework.core.io.ClassPathResource resource = new org.springframework.core.io.ClassPathResource(
                        "serviceAccountKey.json");

                GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream())
                        .createScoped(java.util.Collections
                                .singletonList("https://www.googleapis.com/auth/firebase.messaging"));

                // Proactively test if we can refresh the token to catch auth errors early
                credentials.refreshAccessToken();
                log.info("Firebase credentials verified. Project ID: {}",
                        ((com.google.auth.oauth2.ServiceAccountCredentials) credentials).getProjectId());

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase Cloud Messaging initialized successfully");
            }
        } catch (IOException e) {
            log.error("Error initializing Firebase or Refreshing Token: {}", e.getMessage());
            if (e.getMessage().contains("scope")) {
                log.error(
                        "HINT: The service account may not have permission for FCM. Ensure 'Firebase Admin' role is assigned in IAM.");
            }
        }
    }
}
