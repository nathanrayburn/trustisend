package dev.test.trustisend.bean;

public class Group {
    private String groupUUID;
    private String userEmail;
    private String directory;
    private String timestamp;
    private String numberDownloads;

    public Group(String groupUUID, String userEmail, String directory, String timestamp, String numberDownloads) {
        this.groupUUID = groupUUID;
        this.userEmail = userEmail;
        this.directory = directory;
        this.timestamp = timestamp;
        this.numberDownloads = numberDownloads;
    }


    public String getGroupUUID() {
        return groupUUID;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getDirectory() {
        return directory;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getNumberDownloads() {
        return numberDownloads;
    }
}
