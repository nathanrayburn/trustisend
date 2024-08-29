package dev.test.trustisend.service;


import dev.test.trustisend.dto.FileDto;
import dev.test.trustisend.entity.InputFile;
import dev.test.trustisend.exception.BadRequestException;
import dev.test.trustisend.exception.GCPFileUploadException;
import dev.test.trustisend.util.DataBucketUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


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

        Arrays.asList(files).forEach(file -> {
            String originalFileName = file.getOriginalFilename();
            if(originalFileName == null){
                throw new BadRequestException("Original file name is null");
            }
            Path path = new File(originalFileName).toPath();

            try {
                String contentType = Files.probeContentType(path);
                FileDto fileDto = dataBucketUtil.uploadFile(file, originalFileName, contentType);

                if (fileDto != null) {
                    inputFiles.add(new InputFile(fileDto.getFileName(), fileDto.getFileUrl()));
                }
            } catch (Exception e) {
                throw new GCPFileUploadException("Error occurred while uploading: " + e.getMessage());
            }
        });

        return inputFiles;
    }
}
