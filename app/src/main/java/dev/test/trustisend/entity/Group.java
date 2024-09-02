package dev.test.trustisend.entity;

import dev.test.trustisend.exception.InvalidTimestampFormatException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Group {
    private String groupUUID;
    private String userEmail;
    private String timestamp;
    private Integer numberDownloads;

    public Group(String userEmail,  String timestamp, Integer numberDownloads) {
        this.userEmail = userEmail;
        this.timestamp = validateAndFormatTimestamp(timestamp);
        this.numberDownloads = numberDownloads;
    }

    public Group(String groupUUID, String userEmail, String timestamp, Integer numberDownloads) {
        this.groupUUID = groupUUID;
        this.userEmail = userEmail;
        this.timestamp = validateAndFormatTimestamp(timestamp);
        this.numberDownloads = numberDownloads;
    }

    public String getGroupUUID() {
        return groupUUID;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Integer getNumberDownloads() {
        return numberDownloads;
    }


    private String validateAndFormatTimestamp(String timestamp) {
        try {
            // Define the expected timestamp format
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

            // Parse the input timestamp to validate and format it
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, formatter);

            // Return the formatted timestamp as a string
            return dateTime.format(formatter);
        } catch (DateTimeParseException e) {
            throw new InvalidTimestampFormatException("Invalid timestamp format. Expected format: YYYY-MM-DDTHH:MM:SS");
        }
    }
}
