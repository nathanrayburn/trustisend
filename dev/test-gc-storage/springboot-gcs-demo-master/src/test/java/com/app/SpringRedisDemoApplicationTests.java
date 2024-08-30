package com.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringRedisDemoApplicationTests {

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
	@Test
	void contextLoads() {
		assertNotNull(context, "The application context should have loaded.");
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
