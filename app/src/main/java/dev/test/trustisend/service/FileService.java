package dev.test.trustisend.service;

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

    public List<InputFile> uploadFiles(MultipartFile[] files, String uID) throws IOException {

        List<InputFile> inputFiles = new ArrayList<>();

        for (MultipartFile file : Arrays.asList(files)) {
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null) {
                throw new BadRequestException("Original file name is null");
            }
        }

            FileDto fileDto = dataBucketUtil.uploadFile(file, originalFileName, file.getContentType(), uID);

            if (fileDto != null) {
                inputFiles.add(new InputFile(uID, fileDto.getFileName(), fileDto.getFileUrl()));
            }

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
    public File downloadFolder(String uID){
        if(uID == null || uID.isEmpty()){
            throw new BadRequestException("uID is null");
        }

        return Zipper.zip(dataBucketUtil.downloadFolder(uID), uID);
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
