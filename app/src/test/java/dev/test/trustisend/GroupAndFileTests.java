package dev.test.trustisend;

import dev.test.trustisend.entity.ActiveFile;
import dev.test.trustisend.entity.FileScanStatus;
import dev.test.trustisend.entity.Group;
import dev.test.trustisend.util.DataBucketUtil;
import dev.test.trustisend.util.FirestoreUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GroupAndFileTests {

    @Autowired
    private FirestoreUtil firestoreUtil;

    @Autowired
    private DataBucketUtil dataBucketUtil;

    private Group testGroup;
    private ActiveFile testActiveFile;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize a new Group and ActiveFile before each test
        testGroup = createTestGroup();
        testActiveFile = createTestActiveFile(testGroup);

        // Add any other setup tasks if needed
    }

    @AfterEach
    void tearDown() throws Exception {
        // Cleanup after each test to avoid side effects
        if (testActiveFile != null && testActiveFile.getFileUUID() != null) {
            firestoreUtil.deleteActiveFile(testActiveFile.getFileUUID());
        }
        if (testGroup != null && testGroup.getGroupUUID() != null) {
            firestoreUtil.deleteGroup(testGroup.getGroupUUID());
        }
    }

    @Test
    @Order(1)
    void createFirestoreGroup() {
        try {
            Group createdGroup = firestoreUtil.createGroup(testGroup);
            assertNotNull(createdGroup.getGroupUUID(), "The returned ID should not be null.");
            assertFalse(createdGroup.getGroupUUID().isEmpty(), "The returned ID should not be empty.");
        } catch (Exception e) {
            fail("Exception occurred during group creation: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void createActiveFile() {
        try {
            ActiveFile createdFile = firestoreUtil.createActiveFile(testActiveFile);
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
            firestoreUtil.createActiveFile(testActiveFile);  // Ensure the file exists
            firestoreUtil.deleteActiveFile(testActiveFile.getFileUUID());
            assertNull(firestoreUtil.readActiveFileByUUID(testActiveFile.getFileUUID()), "File should not exist after deletion.");
        } catch (Exception e) {
            fail("Exception occurred during file deletion: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    void deleteFirestoreGroup() {
        try {
            firestoreUtil.createGroup(testGroup);  // Ensure the group exists
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
            Group newGroup = createTestGroup();
            firestoreUtil.createGroup(newGroup);

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

    @Test
    @Order(6)
    void uploadSingleFile() throws IOException {
        Path tempPath = createTempFile("single-upload", ".txt", "Upload test file");
        MultipartFile tempFile = createMultipartFile(tempPath);
        try {
            firestoreUtil.createActiveFile(testActiveFile);  // Ensure the file exists
            dataBucketUtil.uploadFile(tempFile, tempFile.getOriginalFilename(), Files.probeContentType(tempPath), testGroup.getGroupUUID());
        } catch (Exception e) {
            fail("Exception occurred during file upload: " + e.getMessage());
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    @Test
    void downloadMultipleFiles() {
        assertEquals(0, 0);  // Implement this test as needed
    }

    @Test
    void invalidFileTypeUpload() throws IOException {
        Path tempPath = createTempFile("invalid-upload", ".txt", "Upload test file");
        MultipartFile tempFile = createMultipartFile(tempPath);

        assertThrows(Exception.class, () -> {
            dataBucketUtil.uploadFile(tempFile, tempFile.getOriginalFilename(), Files.probeContentType(tempPath));
        });

        Files.deleteIfExists(tempPath);
    }

    private Path createTempFile(String prefix, String suffix, String content) throws IOException {
        Path tempFile = Files.createTempFile(prefix, suffix);
        Files.writeString(tempFile, content, StandardOpenOption.WRITE);
        return tempFile;
    }

    private MultipartFile createMultipartFile(Path path) throws IOException {
        String filename = path.getFileName().toString();
        String contentType = Files.probeContentType(path);
        byte[] content = Files.readAllBytes(path);
        return new MockMultipartFile("file", filename, contentType, content);
    }

    private Group createTestGroup() {
        Group group = new Group("test@heig-vd.ch", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), 0);
        try{
            return firestoreUtil.createGroup(group);
        }catch (Exception e){
            fail("Exception occurred during group creation: " + e.getMessage());
            return null;
        }
    }

    private void deleteTestGroup(Group group) {
        try {
            firestoreUtil.deleteGroupWithDependecies(group.getGroupUUID());
        } catch (Exception e) {
            fail("Exception occurred during group deletion: " + e.getMessage());
        }
    }

    private ActiveFile createTestActiveFile(Group group) {
        ActiveFile activeFile = new ActiveFile(group, "testfile.txt", FileScanStatus.PENDING);
        try{
            return firestoreUtil.createActiveFile(activeFile);
        }catch (Exception e){
            fail("Exception occurred during active file creation: " + e.getMessage());
            return null;
        }

    }
}
