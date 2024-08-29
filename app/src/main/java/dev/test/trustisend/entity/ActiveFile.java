package dev.test.trustisend.entity;

public class ActiveFile extends Group {
    private String path;

    public ActiveFile(String groupUUID, String userEmail, String timestamp, Integer numberDownloads, String path) {
        super(groupUUID, userEmail, timestamp, numberDownloads);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
