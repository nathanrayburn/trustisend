package dev.test.trustisend.service;


import dev.test.trustisend.dto.FileDto;
import dev.test.trustisend.entity.ActiveFile;
import dev.test.trustisend.entity.Group;
import dev.test.trustisend.entity.InputFile;
import dev.test.trustisend.entity.User;
import dev.test.trustisend.exception.BadRequestException;
import dev.test.trustisend.exception.GCPFileUploadException;
import dev.test.trustisend.util.DataBucketUtil;
import dev.test.trustisend.util.FirestoreUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FileService{

    private final DataBucketUtil dataBucketUtil;

    private final FirestoreUtil firestoreUtil;

    public List<InputFile> uploadFiles(MultipartFile[] files) {

        List<InputFile> inputFiles = new ArrayList<>();

        //String uID = java.util.UUID.randomUUID().toString();
        // Create a new group
        // todo some how get the user
        // using fictive user
        User user = new User("test@heig-vd.ch", "testpassword");

        Group group = new Group(user.getEmail(), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), 0);

        try{
            group = firestoreUtil.createGroup(group);

        }catch (Exception e){
            e.printStackTrace();
            throw new BadRequestException("Error occurred while uploading: " + e.getMessage());
        }


        Group finalGroup = group;
        System.out.println("Group created successfully with ID: " + finalGroup.getGroupUUID());
        Arrays.asList(files).forEach(file -> {
            String originalFileName = file.getOriginalFilename();
            if(originalFileName == null){
                throw new BadRequestException("Original file name is null");
            }
            
            Path path = new File(originalFileName).toPath();

            try {

                String contentType = Files.probeContentType(path);
                FileDto fileDto = dataBucketUtil.uploadFile(file, originalFileName, contentType, finalGroup.getGroupUUID());
                // Add file to Firestore
                ActiveFile activeFile = new ActiveFile(finalGroup, originalFileName);
                activeFile = firestoreUtil.createActiveFile(activeFile);

                System.out.println("File created successfully with ID: " + activeFile.getFileUUID());

                if (fileDto != null) {
                    inputFiles.add(new InputFile(finalGroup.getGroupUUID(), fileDto.getFileName(), fileDto.getFileUrl()));
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
