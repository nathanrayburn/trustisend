package dev.test.trustisend.entity;

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

    public InputFile(String uID, String fileName) {
        this.uID = uID;
        this.fileName = fileName;
    }
}




