package dev.test.trustisend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class FirestoreConfig {

    @Value("${project.id}")
    private String projectId;

    @Value("${firebase.database.id}")
    private String databaseID;

    @Bean
    public Firestore firestore() throws IOException {

        String credentialsJson = new String(Files.readAllBytes(Paths.get(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"))));
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ByteArrayInputStream(credentialsJson.getBytes())
        );

        FirestoreOptions firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .setDatabaseId(databaseID)
                .build();


        return firestoreOptions.getService();
    }
}