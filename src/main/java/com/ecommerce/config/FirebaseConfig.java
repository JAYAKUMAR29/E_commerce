package com.ecommerce.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseConfig {

    private static final String DEFAULT_CREDENTIALS_PATH = "src/main/resources/firebase-key.json";
    private static Firestore firestore;
    private static boolean initialized = false;

    private FirebaseConfig() {
        // Utility singleton
    }

    public static synchronized boolean initialize(String credentialsPath) {
        if (initialized && firestore != null) {
            return true;
        }

        String path = (credentialsPath != null && !credentialsPath.isBlank()) ? credentialsPath : DEFAULT_CREDENTIALS_PATH;
        File file = new File(path);

        if (!file.exists()) {
            System.out.println("ℹ️ Firebase credentials file not found at: " + file.getAbsolutePath());
            System.out.println("ℹ️ To connect to live Firestore, place your 'firebase-key.json' in 'src/main/resources/'.");
            return false;
        }

        try (FileInputStream serviceAccount = new FileInputStream(file)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            firestore = FirestoreClient.getFirestore();
            initialized = true;
            System.out.println("✅ Firebase & Cloud Firestore successfully connected!");
            return true;
        } catch (IOException e) {
            System.err.println("❌ Failed to initialize Firebase: " + e.getMessage());
            return false;
        }
    }

    public static Firestore getFirestore() {
        if (!initialized || firestore == null) {
            throw new IllegalStateException("Firebase has not been initialized yet. Call FirebaseConfig.initialize() first.");
        }
        return firestore;
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
