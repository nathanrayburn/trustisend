package dev.test.trustisend;

import dev.test.trustisend.bean.User;
import dev.test.trustisend.util.DataBucketUtil;
import dev.test.trustisend.util.FirestoreUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserTests {


    @Autowired
    private FirestoreUtil firestoreUtil;

    private static String userId;

    @Test
    @Order(1)
    void createFirestoreUser() {
        // Create a test user object
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", "testuser@heig-vd.ch");
        userData.put("hash", "testpassword");  // Replace with hashed password in production

        try {
            // Create a user and get the returned ID
            userId = firestoreUtil.createUser(userData);

            // Assert that the ID is not null or empty, meaning user creation was successful
            Assertions.assertNotNull(userId, "The returned ID should not be null.");
            Assertions.assertFalse(userId.isEmpty(), "The returned ID should not be empty.");

            System.out.println("User created successfully with ID: " + userId);

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception occurred during user creation: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void deleteFirestoreUser() {
        try {
            // Ensure a user ID is available for deletion
            Assertions.assertNotNull(userId, "User ID should not be null before deletion");

            // Attempt to delete the user
            firestoreUtil.deleteUser(userId);

            // Verify user has been deleted by trying to read it (should throw an exception or return null)
            try {
                UserDetails user = firestoreUtil.readUserByEmail("testuser@heig-vd.ch");
                Assertions.assertNull(user, "User should not exist after deletion.");
            } catch (UsernameNotFoundException e) {
                // Expected behavior: user should not be found after deletion
                System.out.println("User successfully deleted and not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception occurred during user deletion: " + e.getMessage());
        }
    }
}
