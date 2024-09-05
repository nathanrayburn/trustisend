package dev.test.trustisend.util;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import dev.test.trustisend.exception.BadRequestException;
import dev.test.trustisend.exception.GCPFileUploadException;

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

    /**
     * dowload a file from gcloud
     * @param uID name of the folder of the file
     * @param fileName name of the file
     * @return downloaded file
     */
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

    /**
     * download a folder from gcloud
     * @param uID name of the folder
     * @return a list of the files in the folder
     */
    public List<File> downloadFolder(String uID) {
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
                throw new BadRequestException("Error creating temporary directory: " + e.getMessage());
            }
        }

        try {
            //get credentials and bucket
            String credentialsJson = new String(Files.readAllBytes(Paths.get(gcpConfigFile)));
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes())
            );
            StorageOptions options = StorageOptions.newBuilder().setProjectId(gcpProjectId)
                    .setCredentials(credentials).build();
            Storage storage = options.getService();
            Bucket bucket = storage.get(gcpBucketId, Storage.BucketGetOption.fields());

            List<File> files = new ArrayList<>();
            // Search the right blobs
            Page<Blob> blobs = bucket.list();
            for (Blob blob : blobs.getValues()) {
                String blobName = blob.getName();
                if (blobName.contains(uID)) {
                    // Define the path for the temporary file using the blob's name
                    Path tempFilePath = tempDirPath.resolve(blobName.substring(blobName.lastIndexOf('/') + 1));

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

    /**
     * delete a file in gcloud
     * @param uID name of the folder of the file
     * @param fileName name of the file
     */
    public boolean deleteFile(String uID, String fileName){
        try{
            //get credentials and bucket
            String credentialsJson = new String(Files.readAllBytes(Paths.get(gcpConfigFile)));
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes())
            );
            StorageOptions options = StorageOptions.newBuilder().setProjectId(gcpProjectId)
                    .setCredentials(credentials).build();
            Storage storage = options.getService();
            Bucket bucket = storage.get(gcpBucketId,Storage.BucketGetOption.fields());

            //get the name of the file in gcloud
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
    /**
     * Uploads a file to Google Cloud Storage using an InputStream.
     *
     * @param fileStream the InputStream of the file to be uploaded
     * @param fileName the name of the file to be stored in the bucket
     * @param contentType the MIME type of the file
     * @param uID the unique identifier for the user's directory
     * @return The name of the file in google cloud
     */
    public String uploadFileStream(InputStream fileStream, String fileName, String contentType, String uID) {
        try (InputStream stream = fileStream) {
            //get credentials
            String credentialsJson = new String(Files.readAllBytes(Paths.get(gcpConfigFile)));
            GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(credentialsJson.getBytes()));
            StorageOptions options = StorageOptions.newBuilder().setProjectId(gcpProjectId).setCredentials(credentials).build();
            Storage storage = options.getService();

            // Create a BlobInfo object for the file
            BlobInfo blobInfo = BlobInfo.newBuilder(gcpBucketId, uID + "/" + fileName)
                    .setContentType(contentType)
                    .build();

            // Upload the file directly from the InputStream
            Blob blob = storage.create(blobInfo, stream);

            if (blob != null) {
                return fileName;
            }
        } catch (Exception e) {
            throw new GCPFileUploadException("An error occurred while storing data to GCS: " + e.getMessage());
        }
        throw new GCPFileUploadException("An error occurred while storing data to GCS");
    }

    /**
     * deletes a folder and its contents from google cloud given its uID (folder name)
     * @param uID
     * @return
     */
    public boolean deleteFolder(String uID){
        try{
            //get credentials and bucket
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
}