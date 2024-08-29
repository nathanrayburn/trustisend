package dev.test.trustisend;

import dev.test.trustisend.entity.ActiveFile;
import dev.test.trustisend.entity.Group;
import dev.test.trustisend.util.FirestoreUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GroupAndFileTests {
    @Autowired
    private FirestoreUtil firestoreUtil;
    private static Group group;
    private static LinkedList<ActiveFile> activeFiles = new LinkedList<>();

    @Test
    @Order(1)
    void createFirestoreGroup() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Group tmp = new Group("test@heig-vd.ch", timestamp , 0);

        try {

             group = firestoreUtil.createGroup(tmp);

            Assertions.assertNotNull(group.getGroupUUID(), "The returned ID should not be null.");
            Assertions.assertFalse(group.getGroupUUID().isEmpty(), "The returned ID should not be empty.");

            System.out.println("User created successfully with ID: " + group.getGroupUUID());
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception occurred during user creation: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void createActiveFile() {
        ActiveFile activeFile = new ActiveFile(group, "file1.txt");
        activeFiles.add(activeFile);
        try {

            ActiveFile activeFile1 = firestoreUtil.createActiveFile(activeFile);

            Assertions.assertNotNull(activeFile1.getFileUUID(), "The returned ID should not be null.");
            Assertions.assertFalse(activeFile1.getFileUUID().isEmpty(), "The returned ID should not be empty.");

            System.out.println("User created successfully with ID: " + activeFile1.getPath());
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception occurred during user creation: " + e.getMessage());
        }
    }


    @Test
    @Order(3)
    void deleteFirestoreGroup() {
        try {
            // Ensure a user ID is available for deletion
            Assertions.assertNotNull(group.getGroupUUID(), "Group ID should not be null before deletion");
            Group tmp = group;
            // Attempt to delete the user
            firestoreUtil.deleteGroup(group.getGroupUUID());

            // Verify user has been deleted by trying to read it (should throw an exception or return null)
            try {
                Group group = firestoreUtil.readGroupByUUID(tmp.getGroupUUID());
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

    // create test to read active files by group uuid
    // todo: implement this test

    @Test
    @Order(4)
    void createMultipleActiveFiles() {
        group = new Group("test@heig-vd.ch", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) , 0);

        try{
            group = firestoreUtil.createGroup(group);
        }catch (Exception e){
            e.printStackTrace();
            Assertions.fail("Exception occurred during group creation: " + e.getMessage());
        }

        ActiveFile activeFile = new ActiveFile(group, "file1.txt");
        ActiveFile activeFile2 = new ActiveFile(group, "file2.txt");

        activeFiles.add(activeFile);
        activeFiles.add(activeFile2);

        try{
            activeFile = firestoreUtil.createActiveFile(activeFile);
            activeFile2 = firestoreUtil.createActiveFile(activeFile2);
            firestoreUtil.deleteGroupWithDependecies(group.getGroupUUID());
            LinkedList<ActiveFile> activeFiles = firestoreUtil.readActiveFilesByGroupUUID(group.getGroupUUID());
            Assertions.assertNull(activeFiles, "Files should not exist after deletion");

        }catch (Exception e){
            e.printStackTrace();
            Assertions.fail("Exception occurred during active file creation: " + e.getMessage());
        }
    }

}
