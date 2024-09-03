package dev.test.trustisend.controller;


import dev.test.trustisend.entity.InputFile;
import dev.test.trustisend.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public List<InputFile> addFile(@RequestParam("files")MultipartFile[] files){
        return fileService.uploadFiles(files);
    }

    @GetMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    ResponseEntity<Resource> getFile(@RequestParam("uid")String uID, @RequestParam("filename")String fileName) throws IOException {

        //
        File file = fileService.downloadFile(uID, fileName);

        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

        return ResponseEntity.ok()
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }


    //@TODO test
    @GetMapping("/{uid}")
    ResponseEntity<Resource> getFolder(@PathVariable String uID) throws IOException {

        File folder = fileService.downloadFolder(uID);

        Path path = Paths.get(folder.getAbsolutePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

        return ResponseEntity.ok()
                .contentLength(folder.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    ResponseEntity<?> deleteFile(@RequestParam("uid")String uID, @RequestParam("filename")String fileName) throws IOException{
        if(fileService.deleteFile(uID, fileName)){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    //@TODO test
    @DeleteMapping("/{uid}")
    ResponseEntity<Resource> deleteFolder(@PathVariable String uID) throws IOException {

        if(fileService.deleteFolder(uID)){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}


