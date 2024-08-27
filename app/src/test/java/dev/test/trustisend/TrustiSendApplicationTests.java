package dev.test.trustisend;

import dev.test.trustisend.util.DataBucketUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TrustiSendApplicationTests {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private DataBucketUtil dataBucketUtil;
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

	// create directory test to upload and download the test files
	@BeforeAll
    static void globalSetup(){

	}

	// clean up and remove test directory
	@AfterAll
	static void globalClean(){

	}
	@Test
	void uploadSingleFile() throws IOException {

		Path tempPath = createTempFile("single-upload", ".txt", "Upload test file");
		MultipartFile tempFile = createMultipartFile(tempPath);

		dataBucketUtil.uploadFile(tempFile, tempFile.getOriginalFilename(), Files.probeContentType(tempPath));

		Files.deleteIfExists(tempPath);
	}
	@Test
	void uploadMultipleFiles(){
		assertEquals(0,0);
	}
	@Test
	void downloadSingleFile(){

		assertEquals(0,0);
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