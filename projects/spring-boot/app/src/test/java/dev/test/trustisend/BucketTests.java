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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BucketTests {

    @Autowired
    private FirestoreUtil firestoreUtil;

    @Autowired
    private DataBucketUtil dataBucketUtil;

    private Group testGroup;
    private ActiveFile testActiveFile;

    @BeforeEach
    void setUp() throws Exception {
        testGroup = createTestGroup();
        testActiveFile = createTestActiveFile(testGroup);

        // Ensure the group and file are created in Firestore
        firestoreUtil.createGroup(testGroup.getUserEmail());
        firestoreUtil.createActiveFile(testActiveFile);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Cleanup Firestore entries after each test
        if (testActiveFile != null && testActiveFile.getFileUUID() != null) {
            deleteTestActiveFile(testActiveFile);
        }
        if (testGroup != null && testGroup.getGroupUUID() != null) {
            deleteTestGroup(testGroup);
        }
    }

    @Test
    @Order(1)
    void uploadSingleFile() throws IOException {
        Path tempPath = createTempFile("single-upload", ".txt", "Upload test file");
        MultipartFile tempFile = createMultipartFile(tempPath);

        try (InputStream inputStream = tempFile.getInputStream()) {
            dataBucketUtil.uploadFileStream(inputStream, tempFile.getOriginalFilename(), Files.probeContentType(tempPath), testGroup.getGroupUUID());
        } catch (Exception e) {
            fail("Exception occurred during file upload: " + e.getMessage());
        } finally {
            Files.deleteIfExists(tempPath);
        }

        dataBucketUtil.deleteFile(testGroup.getGroupUUID(), tempPath.getFileName().toString());
    }

    @Test
    @Order(2)
    void downloadAndDeleteMultipleFiles() throws IOException{
        //setup

        List<String> filenames = new ArrayList<>();

        for(int i = 0; i<2; ++i){
            Path tempPath = createTempFile("multipledownloadtest", ".txt", "Upload test file");

            MultipartFile tempFile = createMultipartFile(tempPath);
            try (InputStream inputStream = tempFile.getInputStream()) {
                firestoreUtil.createActiveFile(testActiveFile);  // Ensure the file exists
                dataBucketUtil.uploadFileStream(inputStream, tempFile.getOriginalFilename(), Files.probeContentType(tempPath), testGroup.getGroupUUID());
            } catch (Exception e) {
                fail("Exception occurred during file upload: " + e.getMessage());
            } finally {
                filenames.add(tempPath.getFileName().toString());
                Files.deleteIfExists(tempPath);
            }
        }

        List<File> files = dataBucketUtil.downloadFolder(testGroup.getGroupUUID());

        assertEquals(files.size(), 2);

        for(File file : files){
            assert(filenames.contains(file.getName()));
        }

        dataBucketUtil.deleteFolder(testGroup.getGroupUUID());
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
        try {
            return firestoreUtil.createGroup("test@heig-vd.ch");
        } catch (Exception e) {
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
        try {
            return firestoreUtil.createActiveFile(activeFile);
        } catch (Exception e) {
            fail("Exception occurred during active file creation: " + e.getMessage());
            return null;
        }
    }

    private void deleteTestActiveFile(ActiveFile activeFile) {
        try {
            firestoreUtil.deleteActiveFile(activeFile.getFileUUID());
        } catch (Exception e) {
            fail("Exception occurred during active file deletion: " + e.getMessage());
        }
    }
}
