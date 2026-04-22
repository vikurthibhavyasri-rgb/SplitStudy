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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public Firestore getFirestore() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount = null;

            // Method 1: Try Environment Variable (Best for Cloud)
            String jsonConfig = System.getenv("FIREBASE_CONFIG_JSON");
            if (jsonConfig != null && !jsonConfig.trim().isEmpty()) {
                logger.info("Initializing Firebase using FIREBASE_CONFIG_JSON environment variable.");
                serviceAccount = new ByteArrayInputStream(jsonConfig.getBytes(StandardCharsets.UTF_8));
            } else {
                // Method 2: Try Local File (Best for Local Development)
                String fileName = "firebase-service-account.json.json";
                if (Files.exists(Paths.get(fileName))) {
                    logger.info("Found Firebase config at: {}", new File(fileName).getAbsolutePath());
                    serviceAccount = new FileInputStream(fileName);
                } else if (Files.exists(Paths.get("/app/" + fileName))) {
                    logger.info("Found Firebase config at: /app/{}", fileName);
                    serviceAccount = new FileInputStream("/app/" + fileName);
                } else {
                    String error = "CRITICAL: Firebase credentials not found! Please provide FIREBASE_CONFIG_JSON env var or " + fileName + " file.";
                    logger.error(error);
                    throw new IOException(error);
                }
            }

            try {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                logger.info("Firebase has been initialized successfully!");
            } catch (Exception e) {
                logger.error("FAILED to initialize Firebase: {}", e.getMessage());
                throw new IOException("Firebase initialization failed", e);
            }
        }
        return FirestoreClient.getFirestore();
    }
}
