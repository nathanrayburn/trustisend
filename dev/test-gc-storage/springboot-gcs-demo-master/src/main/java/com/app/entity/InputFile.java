package com.app.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class InputFile {

    @Id
    @GeneratedValue
    private String uID;
    private String fileName;
    private String fileUrl;

    public InputFile(String uID, String fileName, String fileUrl) {
        this.uID = uID;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }
}




