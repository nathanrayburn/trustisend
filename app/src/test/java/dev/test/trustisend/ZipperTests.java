package dev.test.trustisend;

import dev.test.trustisend.util.Zipper;
import org.junit.jupiter.api.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import static org.junit.jupiter.api.Assertions.*;

public class ZipperTests {

    @Test
    void testZipFunction() throws IOException {
        // Create temporary files
        File tempFile1 = File.createTempFile("tempFile1", ".txt");
        File tempFile2 = File.createTempFile("tempFile2", ".txt");

        // Write some content to the files
        try (FileWriter writer = new FileWriter(tempFile1)) {
            writer.write("This is the content of tempFile1.");
        }
        try (FileWriter writer = new FileWriter(tempFile2)) {
            writer.write("This is the content of tempFile2.");
        }

        // Call the zip function
        File zipFile = Zipper.zip(List.of(tempFile1, tempFile2), "test.zip");

        // Verify the zip file is created
        assertNotNull(zipFile);
        assertTrue(zipFile.exists());

        // Verify the zip file contains the expected entries
        try (ZipFile zip = new ZipFile(zipFile)) {
            assertNotNull(zip.getEntry(tempFile1.getName()));
            assertNotNull(zip.getEntry(tempFile2.getName()));
        }

        // Clean up temporary files
        Files.deleteIfExists(tempFile1.toPath());
        Files.deleteIfExists(tempFile2.toPath());
        Files.deleteIfExists(zipFile.toPath());
    }
}