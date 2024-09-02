package dev.test.trustisend;

import dev.test.trustisend.entity.ActiveFile;
import dev.test.trustisend.entity.FileScanStatus;
import dev.test.trustisend.entity.Group;
import dev.test.trustisend.util.FirestoreUtil;
import dev.test.trustisend.util.DataBucketUtil;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GroupAndFileTests {
    @Autowired
    private FirestoreUtil firestoreUtil;

    @Autowired
    private DataBucketUtil dataBucketUtil;

    private static Group group;
    private static LinkedList<ActiveFile> activeFiles = new LinkedList<>();

    @Test
    @Order(1)
    void createFirestoreGroup() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Group tmp = new Group("test@heig-vd.ch", timestamp, 0);

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
        ActiveFile activeFile = new ActiveFile(group, "file1.txt", FileScanStatus.PENDING);

        try {
            activeFiles.add(firestoreUtil.createActiveFile(activeFile));
            Assertions.assertNotNull(activeFiles.getFirst().getFileUUID(), "The returned ID should not be null.");
            Assertions.assertFalse(activeFiles.getFirst().getFileUUID().isEmpty(), "The returned ID should not be empty.");
            System.out.println("User created successfully with ID: " + activeFiles.getFirst().getPath());
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception occurred during user creation: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    void deleteActiveFile() {
        try {
            Assertions.assertNotNull(activeFiles.getFirst().getFileUUID(), "File ID should not be null before deletion");
            ActiveFile tmp = activeFiles.getFirst();
            firestoreUtil.deleteActiveFile(tmp.getFileUUID());
            try {
                ActiveFile activeFile = firestoreUtil.readActiveFileByUUID(tmp.getFileUUID());
                Assertions.assertNull(activeFile, "File should not exist after deletion.");
            } catch (Exception e) {
                System.out.println("File successfully deleted and not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception occurred during File deletion: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    void deleteFirestoreGroup() {
        try {
            Assertions.assertNotNull(group.getGroupUUID(), "Group ID should not be null before deletion");
            Group tmp = group;
            firestoreUtil.deleteGroup(group.getGroupUUID());
            activeFiles.removeFirst();
            try {
                Group group = firestoreUtil.readGroupByUUID(tmp.getGroupUUID());
                Assertions.assertNull(group, "Group should not exist after deletion.");
            } catch (Exception e) {
                System.out.println("Group successfully deleted and not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception occurred during Group deletion: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    void createMultipleActiveFiles() {
        group = new Group("test@heig-vd.ch", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), 0);
        try {
            group = firestoreUtil.createGroup(group);
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception occurred during group creation: " + e.getMessage());
        }

        ActiveFile activeFile = new ActiveFile(group, "file1.txt", FileScanStatus.PENDING);
        ActiveFile activeFile2 = new ActiveFile(group, "file2.txt", FileScanStatus.PENDING);
        activeFiles.add(activeFile);
        activeFiles.add(activeFile2);

        try {
            activeFile = firestoreUtil.createActiveFile(activeFile);
            activeFile2 = firestoreUtil.createActiveFile(activeFile2);
            firestoreUtil.deleteGroupWithDependecies(group.getGroupUUID());
            LinkedList<ActiveFile> activeFiles = firestoreUtil.readActiveFilesByGroupUUID(group.getGroupUUID());
            Assertions.assertNull(activeFiles, "Files should not exist after deletion");
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception occurred during active file creation: " + e.getMessage());
        }
    }
    private Path createTempFile(String prefix, String suffix, String content) throws IOException {
        Path tempFile = Files.createTempFile(prefix,suffix);
        Files.writeString(tempFile, content, StandardOpenOption.WRITE);

        return tempFile;
    }
    private MultipartFile createMultipartFile(Path path) throws IOException{
        String filename = path.getFileName().toString();
        String contentType = Files.probeContentType(path);
        byte[] content = Files.readAllBytes(path);
        return new MockMultipartFile("file", filename, contentType, content);

    }
    @Test
    @Order(6)
    void uploadSingleFile() throws IOException {

        Path tempPath = createTempFile("single-upload", ".txt", "Upload test file");
        MultipartFile tempFile = createMultipartFile(tempPath);
        try{
            ActiveFile activeFile = new ActiveFile(group, tempPath.getFileName().toString(), FileScanStatus.PENDING);
            firestoreUtil.createActiveFile(activeFile);
            dataBucketUtil.uploadFile(tempFile, tempFile.getOriginalFilename(), Files.probeContentType(tempPath), group.getGroupUUID());


        }catch (Exception e){
            e.printStackTrace();
            Assertions.fail("Exception occurred during file upload: " + e.getMessage());
        }


        Files.deleteIfExists(tempPath);
    }

    @Test
    void downloadMultipleFiles(){
        assertEquals(0,0);
    }

    @Test
    void invalidFileTypeUpload() throws IOException {
        Path tempPath = createTempFile("single-upload", ".txt", "Upload test file");
        MultipartFile tempFile = createMultipartFile(tempPath);

        assertThrows(Exception.class, () -> {
            dataBucketUtil.uploadFile(tempFile, tempFile.getOriginalFilename(), Files.probeContentType(tempPath));
        });

        Files.deleteIfExists(tempPath);
    }
}