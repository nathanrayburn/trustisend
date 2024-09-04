package dev.test.trustisend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import dev.test.trustisend.dto.FileDto;
import dev.test.trustisend.entity.InputFile;
import dev.test.trustisend.exception.BadRequestException;
import dev.test.trustisend.exception.GCPFileUploadException;
import dev.test.trustisend.util.DataBucketUtil;
import dev.test.trustisend.util.Zipper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService{

    @Autowired
    private final DataBucketUtil dataBucketUtil;


    public List<InputFile> uploadFiles(MultipartFile[] files, String uID) {
        List<InputFile> inputFiles = new ArrayList<>();

        // Determine the current working directory
        String currentDir = System.getProperty("user.dir");
        // Create a temporary directory within the current working directory
        String uniqueTempDir = "temp_" + uID;
        Path tempDirPath = Paths.get(currentDir, uniqueTempDir);

        // Create the temporary directory if it doesn't exist
        if (!Files.exists(tempDirPath)) {
            try {
                Files.createDirectories(tempDirPath);
            } catch (IOException e) {
                throw new GCPFileUploadException("Error creating temporary directory: " + e.getMessage());
            }
        }

        Arrays.asList(files).forEach(multipartFile -> {
            String originalFileName = multipartFile.getOriginalFilename();
            if (originalFileName == null || originalFileName.trim().isEmpty()) {
                throw new BadRequestException("Original file name is null or empty");
            }

            try {
                // Define the path for the temporary file using the original file name
                Path tempFilePath = tempDirPath.resolve(originalFileName);

                // Ensure that any existing temporary file is deleted before creating a new one
                if (Files.exists(tempFilePath)) {
                    Files.delete(tempFilePath);
                }

                // Save the file to the temporary directory using try-with-resources
                try (FileOutputStream outputStream = new FileOutputStream(tempFilePath.toFile())) {
                    outputStream.write(multipartFile.getBytes());
                }

                String contentType = Files.probeContentType(tempFilePath);
                // Pass the temporary file to DataBucketUtil, use the updated convertFile method
                FileDto fileDto = dataBucketUtil.uploadFileUsingTempFile(tempFilePath.toFile(), originalFileName, contentType, uID);

                if (fileDto != null) {
                    inputFiles.add(new InputFile(uID, fileDto.getFileName(), fileDto.getFileUrl()));
                }

                // Clean up the temporary file after successful upload
                Files.deleteIfExists(tempFilePath);

            } catch (IOException e) {
                throw new GCPFileUploadException("Error occurred while handling file: " + e.getMessage());
            } catch (Exception e) {
                throw new GCPFileUploadException("Unexpected error occurred: " + e.getMessage());
            }
        });

        // Optional: Clean up the temporary directory after all files are processed
        try {
            Files.deleteIfExists(tempDirPath);
        } catch (IOException e) {
            // Log a warning if unable to delete, but don't throw an exception
            System.err.println("Warning: Unable to delete temporary directory: " + e.getMessage());
        }

        return inputFiles;
    }
    public File downloadFile(String uID, String fileName){

        if(fileName == null){
            throw new BadRequestException("filename is null");
        }
        if(uID == null){
            uID = "";
        }
        return dataBucketUtil.downloadFile(uID, fileName);
    }

    //@TODO test
    public File downloadFolder(String uID) {
        if (uID == null || uID.isEmpty()) {
            throw new BadRequestException("uID is null");
        }

        // Use the system's default temporary directory and create a subdirectory named after the unique uID
        String tempDir = System.getProperty("java.io.tmpdir");
        String uniqueTempDir = "temp_" + uID;
        Path tempDirPath = Paths.get(tempDir, uniqueTempDir);

        // Create the temporary directory if it doesn't exist
        if (!Files.exists(tempDirPath)) {
            try {
                Files.createDirectories(tempDirPath);
            } catch (IOException e) {
                throw new BadRequestException("Error creating temporary directory: " + e.getMessage());
            }
        }

        // Get the list of files from the DataBucketUtil
        List<File> files = dataBucketUtil.downloadFolder(uID);

        // Define the path for the zip file within the temporary directory
        Path zipFilePath = tempDirPath.resolve(uID + "_folder.zip");

        // Ensure that any existing zip file is deleted before creating a new one
        try {
            if (Files.exists(zipFilePath)) {
                Files.delete(zipFilePath);
            }
        } catch (IOException e) {
            throw new BadRequestException("Error deleting existing zip file: " + e.getMessage());
        }

        // Create the zip file in the temporary directory
        File zipFile = Zipper.zip(files, zipFilePath.toString());

        // Return the zip file created in the temporary directory
        return zipFile;
    }


    public boolean deleteFile(String uID, String fileName){
        if(fileName == null){
            throw new BadRequestException("filename is null");
        }
        if(uID == null){
            uID = "";
        }
        return dataBucketUtil.deleteFile(uID, fileName);
    }

    //@TODO test
    public boolean deleteFolder(String uID){
        if(uID == null || uID.isEmpty()){
            throw new BadRequestException("uID is empty or null");
        }
        return dataBucketUtil.deleteFolder(uID);
    }
}
