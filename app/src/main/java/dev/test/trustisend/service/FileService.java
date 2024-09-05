package dev.test.trustisend.service;

import dev.test.trustisend.entity.ActiveFile;
import dev.test.trustisend.entity.FileScanStatus;
import dev.test.trustisend.entity.Group;
import dev.test.trustisend.util.FirestoreUtil;
import dev.test.trustisend.entity.InputFile;
import dev.test.trustisend.exception.BadRequestException;
import dev.test.trustisend.exception.GCPFileUploadException;
import dev.test.trustisend.util.DataBucketUtil;
import dev.test.trustisend.util.Zipper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
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
    @Autowired
    private FirestoreUtil firestoreUtil;

    /**
     * upload a list of files to gcloud and logs them in firestoredb
     * @param files list of files to upload
     * @param uID name of the folder in which to upload files
     * @return list of InputFiles objects that contain the file name and folder name of the files successfully uploaded
     */
    public List<InputFile> uploadFiles(MultipartFile[] files, String uID) {
        List<InputFile> inputFiles = new ArrayList<>();

        Arrays.asList(files).forEach(multipartFile -> {
            String originalFileName = multipartFile.getOriginalFilename();
            if (originalFileName == null || originalFileName.trim().isEmpty()) {
                throw new BadRequestException("Original file name is null or empty");
            }

            try (InputStream inputStream = multipartFile.getInputStream()) {
                String contentType = multipartFile.getContentType();

                //upload file
                String fileName = dataBucketUtil.uploadFileStream(inputStream, originalFileName, contentType, uID);

                //update the database
                if (fileName != null) {
                    inputFiles.add(new InputFile(uID, fileName));
                    Group group = firestoreUtil.readGroupByUUID(uID);
                    ActiveFile activeFile = new ActiveFile(group, fileName, FileScanStatus.PENDING);
                    activeFile = firestoreUtil.createActiveFile(activeFile);
                }
            } catch (IOException e) {
                throw new GCPFileUploadException("Error occurred while handling file stream: " + e.getMessage());
            } catch (Exception e) {
                throw new GCPFileUploadException("Unexpected error occurred: " + e.getMessage());
            }
        });

        return inputFiles;
    }

    /**
     * download a single file from gcloud
     * @param uID name of the folder
     * @param fileName name of the file
     * @return the downloaded file
     */
    public File downloadFile(String uID, String fileName){

        if(fileName == null){
            throw new BadRequestException("filename is null");
        }
        if(uID == null){
            uID = "";
        }
        return dataBucketUtil.downloadFile(uID, fileName);
    }

    /**
     * download a folder as a zip file from gcloud
     * @param uID name of the folder
     * @return a zip file containing all files in the folder
     */
    public File downloadFolder(String uID) {
        if (uID == null || uID.isEmpty()) {
            throw new BadRequestException("uID is null");
        }

        // Use the current working directory and create a subdirectory named after the unique uID
        String currentDir = System.getProperty("user.dir");
        String uniqueTempDir = "temp_" + uID;
        Path tempDirPath = Paths.get(currentDir, uniqueTempDir);

        // Define the path for the zip file within the temporary directory
        Path zipFilePath = tempDirPath.resolve(uID + "_folder.zip");

        // Check if the zip file already exists
        if (Files.exists(zipFilePath)) {
            // If the zip file exists, return it directly
            return zipFilePath.toFile();
        }

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

        // Create the zip file in the temporary directory
        File zipFile = Zipper.zip(files, zipFilePath.toString());

        // Clean up temporary files and directory after zipping
        try {
            // Delete individual temporary files
            for (File file : files) {
                Files.deleteIfExists(file.toPath());
            }

        } catch (IOException e) {
            System.err.println("Warning: Unable to delete temporary files or directory: " + e.getMessage());
        }

        // Return the zip file created in the temporary directory
        return zipFile;
    }


    /**
     * delete a file from gcloud
     * @param uID name of the folder
     * @param fileName name of the file
     */
    public boolean deleteFile(String uID, String fileName){
        if(fileName == null){
            throw new BadRequestException("filename is null");
        }
        if(uID == null){
            uID = "";
        }
        return dataBucketUtil.deleteFile(uID, fileName);
    }

    /**
     * delete a folder and its contents from gcloud
     * @param uID name of the folder
     */
    public boolean deleteFolder(String uID){
        if(uID == null || uID.isEmpty()){
            throw new BadRequestException("uID is empty or null");
        }
        return dataBucketUtil.deleteFolder(uID);
    }
}
