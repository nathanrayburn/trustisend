package dev.test.trustisend.entity;

public class ActiveFile extends Group {
    private String path;
    private String fileUUID;
    public ActiveFile(String groupUUID, String userEmail, String timestamp, Integer numberDownloads, String fileUUID, String path) {
        super(groupUUID, userEmail, timestamp, numberDownloads);
        this.fileUUID = fileUUID;
        this.path = path;
    }
    public ActiveFile(Group group, String fileUUID, String path) {
        super(group.getGroupUUID(), group.getUserEmail(), group.getTimestamp(), group.getNumberDownloads());
        this.fileUUID = fileUUID;
        this.path = path;
    }
    public ActiveFile(Group group, String path) {
        super(group.getGroupUUID(), group.getUserEmail(), group.getTimestamp(), group.getNumberDownloads());
        this.path = path;
    }

    public String getFileUUID() {
        return fileUUID;
    }
    public String getPath() {
        return path;
    }
}
