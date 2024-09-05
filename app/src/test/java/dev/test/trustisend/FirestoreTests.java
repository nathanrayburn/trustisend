package dev.test.trustisend;

import dev.test.trustisend.entity.ActiveFile;
import dev.test.trustisend.entity.FileScanStatus;
import dev.test.trustisend.entity.Group;
import dev.test.trustisend.util.FirestoreUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FirestoreTests {

    @Autowired
    private FirestoreUtil firestoreUtil;

    private Group testGroup;

    @Test
    @Order(1)
    void createFirestoreGroup() {
        try {
            testGroup = firestoreUtil.createGroup(getTestGroupEmail());
            assertNotNull(testGroup.getGroupUUID(), "The returned ID should not be null.");
            assertFalse(testGroup.getGroupUUID().isEmpty(), "The returned ID should not be empty.");
        } catch (Exception e) {
            fail("Exception occurred during group creation: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void createActiveFile() {
        try {

            Group createdGroup = firestoreUtil.createGroup(getTestGroupEmail());
            ActiveFile testFile = new ActiveFile(createdGroup, "testfile.txt", FileScanStatus.PENDING);
            ActiveFile createdFile = firestoreUtil.createActiveFile(testFile);
            assertNotNull(createdFile.getFileUUID(), "The returned ID should not be null.");
            assertFalse(createdFile.getFileUUID().isEmpty(), "The returned ID should not be empty.");
        } catch (Exception e) {
            fail("Exception occurred during active file creation: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    void deleteActiveFile() {
        try {
            Group createdGroup = firestoreUtil.createGroup(getTestGroupEmail());
            ActiveFile testFile = new ActiveFile(createdGroup, "testfile.txt", FileScanStatus.PENDING);
            ActiveFile createdFile = firestoreUtil.createActiveFile(testFile);
            firestoreUtil.createActiveFile(createdFile);  // Ensure the file exists
            firestoreUtil.deleteActiveFile(createdFile.getFileUUID());
            assertNull(firestoreUtil.readActiveFileByUUID(createdFile.getFileUUID()), "File should not exist after deletion.");
        } catch (Exception e) {
            fail("Exception occurred during file deletion: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    void deleteFirestoreGroup() {
        try {
            createFirestoreGroup();
            firestoreUtil.createGroup(testGroup.getUserEmail());  // Ensure the group exists
            firestoreUtil.deleteGroup(testGroup.getGroupUUID());
            assertNull(firestoreUtil.readGroupByUUID(testGroup.getGroupUUID()), "Group should not exist after deletion.");
        } catch (Exception e) {
            fail("Exception occurred during group deletion: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    void createMultipleActiveFiles() {
        try {
            Group newGroup = firestoreUtil.createGroup(getTestGroupEmail());

            ActiveFile file1 = createTestActiveFile(newGroup);
            ActiveFile file2 = new ActiveFile(newGroup, "file2.txt", FileScanStatus.PENDING);

            firestoreUtil.createActiveFile(file1);
            firestoreUtil.createActiveFile(file2);

            firestoreUtil.deleteGroupWithDependecies(newGroup.getGroupUUID());

            assertNull(firestoreUtil.readActiveFilesByGroupUUID(newGroup.getGroupUUID()), "Files should not exist after deletion");
        } catch (Exception e) {
            fail("Exception occurred during multiple active file creation: " + e.getMessage());
        }
    }

    private String getTestGroupEmail() {
        return "test@heig-vd.ch";
    }

    private ActiveFile createTestActiveFile(Group group) {
        return new ActiveFile(group, "testfile.txt", FileScanStatus.PENDING);
    }
}
