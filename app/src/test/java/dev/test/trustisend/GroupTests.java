package dev.test.trustisend;

import dev.test.trustisend.entity.Group;
import dev.test.trustisend.util.FirestoreUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@SpringBootTest
public class GroupTests {
    @Autowired
    private FirestoreUtil firestoreUtil;
    private static String groupUUID;

    @Test
    @Order(1)
    void createFirestoreGroup() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Group group = new Group("test@heig-vd.ch", timestamp , 0);

        try {
            // Create a user and get the returned ID
            Group group1 = firestoreUtil.createGroup(group);

            // Assert that the ID is not null or empty, meaning user creation was successful
            Assertions.assertNotNull(group1.getGroupUUID(), "The returned ID should not be null.");
            Assertions.assertFalse(group1.getGroupUUID().isEmpty(), "The returned ID should not be empty.");

            System.out.println("User created successfully with ID: " + group1.getGroupUUID());
            groupUUID = group1.getGroupUUID();
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception occurred during user creation: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void deleteFirestoreGroup() {
        try {
            // Ensure a user ID is available for deletion
            Assertions.assertNotNull(groupUUID, "Group ID should not be null before deletion");

            // Attempt to delete the user
            firestoreUtil.deleteGroup(groupUUID);

            // Verify user has been deleted by trying to read it (should throw an exception or return null)
            try {
                Group group = firestoreUtil.readGroupByUUID(groupUUID);
                Assertions.assertNull(group, "Group should not exist after deletion.");
            } catch (Exception e) {
                // Expected behavior: user should not be found after deletion
                System.out.println("Group successfully deleted and not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception occurred during Group deletion: " + e.getMessage());
        }
    }


}
