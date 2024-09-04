package dev.test.trustisend.util;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import dev.test.trustisend.dto.FileDto;
import dev.test.trustisend.exception.BadRequestException;
import dev.test.trustisend.exception.FileWriteException;
import dev.test.trustisend.exception.GCPFileUploadException;
import dev.test.trustisend.exception.InvalidFileTypeException;

@Component
public class DataBucketUtil {

    @Value("${gcp.credentials.path}")
    private String gcpConfigFile;

    @Value("${gcp.project.id}")
    private String gcpProjectId;

    @Value("${gcp.bucket.id}")
    private String gcpBucketId;

    @Value("${gcp.dir.name}")
    private String gcpDirectoryName;

    @Value("${app.temp.dir}")
    private String tempDirPath;


    public FileDto uploadFile(MultipartFile multipartFile, String fileName, String contentType) {
        String uID = java.util.UUID.randomUUID().toString();

        return uploadFile(multipartFile, fileName, contentType, uID);
    }
    public FileDto uploadFile(MultipartFile multipartFile, String fileName, String contentType, String uID) {

     try{

         byte[] fileData = FileUtils.readFileToByteArray(convertFile(multipartFile));

         String credentialsJson = new String(Files.readAllBytes(Paths.get(gcpConfigFile)));

         GoogleCredentials credentials = GoogleCredentials.fromStream(
                 new ByteArrayInputStream(credentialsJson.getBytes())
         );

         StorageOptions options = StorageOptions.newBuilder().setProjectId(gcpProjectId)
                 .setCredentials(credentials).build();

         Storage storage = options.getService();
         Bucket bucket = storage.get(gcpBucketId,Storage.BucketGetOption.fields());

         Blob blob = bucket.create( uID + "/" + fileName, fileData, contentType);

         if(blob != null){
             String[] name = blob.getName().split("/");
             return new FileDto(fileName, blob.getMediaLink());
         }

     }catch (Exception e){
         throw new GCPFileUploadException("An error occurred while storing data to GCS: " + e.getMessage());
     }
     throw new GCPFileUploadException("An error occurred while storing data to GCS");
    }

    public FileDto uploadFileUsingTempFile(File tempFile, String fileName, String contentType, String uID) {
        try {
            // Use the temporary file's data directly
            byte[] fileData = FileUtils.readFileToByteArray(tempFile);

            String credentialsJson = new String(Files.readAllBytes(Paths.get(gcpConfigFile)));
            GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(credentialsJson.getBytes()));
            StorageOptions options = StorageOptions.newBuilder().setProjectId(gcpProjectId).setCredentials(credentials).build();

            Storage storage = options.getService();
            Bucket bucket = storage.get(gcpBucketId, Storage.BucketGetOption.fields());

            Blob blob = bucket.create(uID + "/" + fileName, fileData, contentType);

            if (blob != null) {
                return new FileDto(fileName, blob.getMediaLink());
            }

        } catch (Exception e) {
            throw new GCPFileUploadException("An error occurred while storing data to GCS: " + e.getMessage());
        }
        throw new GCPFileUploadException("An error occurred while storing data to GCS");
    }

    public File downloadFile(String uID, String fileName){
        try{
            String credentialsJson = new String(Files.readAllBytes(Paths.get(gcpConfigFile)));

            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes())
            );

            StorageOptions options = StorageOptions.newBuilder().setProjectId(gcpProjectId)
                    .setCredentials(credentials).build();

            Storage storage = options.getService();
            Bucket bucket = storage.get(gcpBucketId,Storage.BucketGetOption.fields());

            String name = fileName;
            if(!uID.isEmpty()){
                name = uID + "/" + fileName;
            }

            //search the right blob
            Page<Blob> blobs = bucket.list();
            for (Blob blob: blobs.getValues()) {
                if (name.equals(blob.getName())) {
                    File downloadedFile = new File(fileName);
                    FileOutputStream outputStream = new FileOutputStream(downloadedFile);
                    outputStream.write(blob.getContent());
                    outputStream.close();
                    return downloadedFile;
                }
            }
        }catch(Exception e){

        }
        throw new BadRequestException("download failed");
    }

    //@TODO test
    public List<File> downloadFolder(String uID) {
        // Use the custom temporary directory and create a subdirectory named after the unique uID
        String uniqueTempDir = "temp_" + uID;
        Path customTempDirPath = Paths.get(tempDirPath, uniqueTempDir); // Use custom temp directory

        // Create the temporary directory if it doesn't exist
        if (!Files.exists(customTempDirPath)) {
            try {
                Files.createDirectories(customTempDirPath);
            } catch (IOException e) {
                throw new BadRequestException("Error creating temporary directory: " + e.getMessage());
            }
        }

        try {
            // Same logic as before to interact with Google Cloud Storage
            String credentialsJson = new String(Files.readAllBytes(Paths.get(gcpConfigFile)));
            GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(credentialsJson.getBytes()));
            StorageOptions options = StorageOptions.newBuilder().setProjectId(gcpProjectId).setCredentials(credentials).build();
            Storage storage = options.getService();
            Bucket bucket = storage.get(gcpBucketId, Storage.BucketGetOption.fields());

            List<File> files = new ArrayList<>();
            // Search the right blobs
            Page<Blob> blobs = bucket.list();
            for (Blob blob : blobs.getValues()) {
                String blobName = blob.getName();
                if (blobName.contains(uID)) {
                    // Define the path for the temporary file using the blob's name
                    Path tempFilePath = customTempDirPath.resolve(blobName.substring(blobName.lastIndexOf('/') + 1));

                    // Ensure that any existing temporary file is deleted before creating a new one
                    if (Files.exists(tempFilePath)) {
                        Files.delete(tempFilePath);
                    }

                    // Save the blob content to the temporary file
                    try (FileOutputStream outputStream = new FileOutputStream(tempFilePath.toFile())) {
                        outputStream.write(blob.getContent());
                    }

                    // Add the temporary file to the list of files
                    files.add(tempFilePath.toFile());
                }
            }
            return files;
        } catch (Exception e) {
            throw new BadRequestException("Download failed: " + e.getMessage());
        }
    }


    public boolean deleteFile(String uID, String fileName){
        try{
            String credentialsJson = new String(Files.readAllBytes(Paths.get(gcpConfigFile)));

            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes())
            );

            StorageOptions options = StorageOptions.newBuilder().setProjectId(gcpProjectId)
                    .setCredentials(credentials).build();

            Storage storage = options.getService();
            Bucket bucket = storage.get(gcpBucketId,Storage.BucketGetOption.fields());

            String name = fileName;
            if(!uID.isEmpty()){
                name = uID + "/" + fileName;
            }

            //search the right blob
            Page<Blob> blobs = bucket.list();
            for (Blob blob: blobs.getValues()) {
                if (name.equals(blob.getName())) {
                    return blob.delete();
                }
            }

        }catch(Exception e){
            throw new BadRequestException("error while deleting");
        }
        throw new BadRequestException("error while deleting");
    }

    //@TODO test
    public boolean deleteFolder(String uID){
        try{
            String credentialsJson = new String(Files.readAllBytes(Paths.get(gcpConfigFile)));

            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes())
            );

            StorageOptions options = StorageOptions.newBuilder().setProjectId(gcpProjectId)
                    .setCredentials(credentials).build();

            Storage storage = options.getService();
            Bucket bucket = storage.get(gcpBucketId,Storage.BucketGetOption.fields());

            //search the right blobs
            Page<Blob> blobs = bucket.list();
            for (Blob blob: blobs.getValues()) {
                String blobName = blob.getName();
                if (blobName.contains(uID)) {
                    blob.delete();
                }
            }
            return true;

        }catch(Exception e){
            throw new BadRequestException("error while deleting");
        }
    }

    /**
     * converts MultipartFile object into a file object
     * @param file
     * @return
     */
    private File convertFile(MultipartFile file) {

        try{
            if(file.getOriginalFilename() == null){
                throw new BadRequestException("Original file name is null");
            }
            File convertedFile = new File(file.getOriginalFilename());
            FileOutputStream outputStream = new FileOutputStream(convertedFile);
            outputStream.write(file.getBytes());
            outputStream.close();
            return convertedFile;
        }catch (Exception e){
            throw new FileWriteException("An error has occurred while converting the file");
        }
    }


    /**
     * Return the file extension, throws an exception if the file name has no extension
     * @param fileName
     * @return String file extension
     */
    private String checkFileExtension(String fileName) {
        if(fileName != null && fileName.contains(".")){
            int i = fileName.lastIndexOf('.');
            String extension = fileName.substring(i);
            return extension;
        }
        throw new InvalidFileTypeException("Not a permitted file type");
    }
}
