package dev.test.trustisend.util;


import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import dev.test.trustisend.dto.FileDto;
import dev.test.trustisend.exception.BadRequestException;
import dev.test.trustisend.exception.FileWriteException;
import dev.test.trustisend.exception.GCPFileUploadException;
import dev.test.trustisend.exception.InvalidFileTypeException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.io.*;
import java.util.concurrent.ThreadLocalRandom;

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


    public FileDto uploadFile(MultipartFile multipartFile, String fileName, String contentType) {

        try{
            byte[] fileData = FileUtils.readFileToByteArray(convertFile(multipartFile));

            File file = ResourceUtils.getFile(gcpConfigFile);
            InputStream inputStream = new FileInputStream(file);
            StorageOptions options = StorageOptions.newBuilder().setProjectId(gcpProjectId)
                    .setCredentials(GoogleCredentials.fromStream(inputStream)).build();

            Storage storage = options.getService();
            Bucket bucket = storage.get(gcpBucketId,Storage.BucketGetOption.fields());

            String id = UUID.randomUUID().toString();
            Blob blob = bucket.create( fileName + "-" + id + checkFileExtension(fileName), fileData, contentType);

            if(blob != null){
                return new FileDto(blob.getName(), blob.getMediaLink());
            }

        }catch (Exception e){
            throw new GCPFileUploadException("An error occurred while storing data to GCS");
        }
        throw new GCPFileUploadException("An error occurred while storing data to GCS");
    }

    public File downloadFile(String uID, String fileName){
        try{
            File file = ResourceUtils.getFile(gcpConfigFile);
            InputStream inputStream = new FileInputStream(file);

            StorageOptions options = StorageOptions.newBuilder().setProjectId(gcpProjectId)
                    .setCredentials(GoogleCredentials.fromStream(inputStream)).build();

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

    public boolean deleteFile(String uID, String fileName){
        try{
            InputStream inputStream = new ClassPathResource(gcpConfigFile).getInputStream();

            StorageOptions options = StorageOptions.newBuilder().setProjectId(gcpProjectId)
                    .setCredentials(GoogleCredentials.fromStream(inputStream)).build();

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
