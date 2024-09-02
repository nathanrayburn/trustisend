package com.app.service;

import com.app.dto.FileDto;
import com.app.entity.InputFile;
import com.app.exception.BadRequestException;
import com.app.exception.GCPFileUploadException;
import com.app.util.DataBucketUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FileService{

    private final DataBucketUtil dataBucketUtil;

    public List<InputFile> uploadFiles(MultipartFile[] files) {

        List<InputFile> inputFiles = new ArrayList<>();

        String uID = java.util.UUID.randomUUID().toString();

        Arrays.asList(files).forEach(file -> {
            String originalFileName = file.getOriginalFilename();
            if(originalFileName == null){
                throw new BadRequestException("Original file name is null");
            }
            
            Path path = new File(originalFileName).toPath();

            try {
                String contentType = Files.probeContentType(path);
                FileDto fileDto = dataBucketUtil.uploadFile(file, originalFileName, contentType, uID);

                if (fileDto != null) {
                    inputFiles.add(new InputFile(uID, fileDto.getFileName(), fileDto.getFileUrl()));
                }
            } catch (Exception e) {
                throw new GCPFileUploadException("Error occurred while uploading: " + e.getMessage());
            }
        });

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

    public boolean deleteFile(String uID, String fileName){
        if(fileName == null){
            throw new BadRequestException("filename is null");
        }
        if(uID == null){
            uID = "";
        }
        return dataBucketUtil.deleteFile(uID, fileName);
    }
}
