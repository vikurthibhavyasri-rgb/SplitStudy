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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public Firestore getFirestore() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            String fileName = "firebase-service-account.json.json";
            InputStream serviceAccount = null;

            // Try to find the file in the current directory or /app directory
            if (Files.exists(Paths.get(fileName))) {
                logger.info("Found Firebase config at: {}", new File(fileName).getAbsolutePath());
                serviceAccount = new FileInputStream(fileName);
            } else if (Files.exists(Paths.get("/app/" + fileName))) {
                logger.info("Found Firebase config at: /app/{}", fileName);
                serviceAccount = new FileInputStream("/app/" + fileName);
            } else {
                String error = "CRITICAL: Firebase service account file '" + fileName + "' NOT FOUND in /app/ or root directory.";
                logger.error(error);
                throw new IOException(error);
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
