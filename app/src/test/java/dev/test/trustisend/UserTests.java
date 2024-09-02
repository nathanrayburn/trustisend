package dev.test.trustisend;

import dev.test.trustisend.util.FirestoreUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import dev.test.trustisend.entity.User;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserTests {


    @Autowired
    private FirestoreUtil firestoreUtil;

    private static String userId;

    @Test
    @Order(1)
    void createFirestoreUser() {
        User user = new User("testuser@heig-vd.ch", "testpassword");
        try {
            // Create a user and get the returned ID
            User newUser = firestoreUtil.createUser(user);
            userId = newUser.getId();

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
