package dev.test.trustisend.util;


import dev.test.trustisend.dto.FileDto;
import dev.test.trustisend.exception.BadRequestException;
import dev.test.trustisend.exception.FileWriteException;
import dev.test.trustisend.exception.InvalidFileTypeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Component
public class DataBucketUtil {

    @Value("${gcp.config.file}")
    private String gcpConfigFile;

    @Value("${gcp.project.id}")
    private String gcpProjectId;

    @Value("${gcp.bucket.id}")
    private String gcpBucketId;

    @Value("${gcp.dir.name}")
    private String gcpDirectoryName;


    public FileDto uploadFile(MultipartFile multipartFile, String fileName, String contentType) {
        return null; /*
        try{
            byte[] fileData = FileUtils.readFileToByteArray(convertFile(multipartFile));

            InputStream inputStream = new ClassPathResource(gcpConfigFile).getInputStream();

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
        */
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
