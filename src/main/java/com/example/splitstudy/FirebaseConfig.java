package com.example.splitstudy;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public Firestore getFirestore() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = null;

                String jsonConfig = System.getenv("FIREBASE_CONFIG_JSON");
                if (jsonConfig != null && !jsonConfig.trim().isEmpty()) {
                    logger.info("Firebase: Using Environment Variable FIREBASE_CONFIG_JSON");
                    serviceAccount = new ByteArrayInputStream(jsonConfig.getBytes(StandardCharsets.UTF_8));
                } else {
                    String fileName = "firebase-service-account.json.json";
                    if (Files.exists(Paths.get(fileName))) {
                        logger.info("Firebase: Found local file at {}", fileName);
                        serviceAccount = new FileInputStream(fileName);
                    } else if (Files.exists(Paths.get("/app/" + fileName))) {
                        logger.info("Firebase: Found cloud file at /app/{}", fileName);
                        serviceAccount = new FileInputStream("/app/" + fileName);
                    }
                }

                if (serviceAccount == null) {
                    logger.error("CRITICAL ERROR: No Firebase credentials found! Application will start without Notebook support.");
                    return null;
                }

                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                logger.info("Firebase initialized successfully!");
            }
            return FirestoreClient.getFirestore();
        } catch (Exception e) {
            logger.error("Firebase Initialization Failed: {}", e.getMessage());
            return null; // Return null instead of crashing
        }
    }
}
