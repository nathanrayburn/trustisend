package dev.test.trustisend.entity;

public class ActiveFile extends Group {
    private final String path;
    private String fileUUID;
    private final FileScanStatus scanStatus;
    public ActiveFile(String groupUUID, String userEmail, String timestamp, Integer numberDownloads, String fileUUID, String path, FileScanStatus scanStatus) {
        super(groupUUID, userEmail, timestamp, numberDownloads);
        this.fileUUID = fileUUID;
        this.path = path;
        this.scanStatus = scanStatus;
    }
    public ActiveFile(Group group, String fileUUID, String path, FileScanStatus scanStatus) {
        super(group.getGroupUUID(), group.getUserEmail(), group.getTimestamp(), group.getNumberDownloads());
        this.fileUUID = fileUUID;
        this.path = path;
        this.scanStatus = scanStatus;
    }
    public ActiveFile(Group group, String path, FileScanStatus scanStatus) {
        super(group.getGroupUUID(), group.getUserEmail(), group.getTimestamp(), group.getNumberDownloads());
        this.path = path;
        this.scanStatus = scanStatus;
    }

    public String getFileUUID() {
        return fileUUID;
    }
    public String getPath() {
        return path;
    }
    public FileScanStatus getScanStatus() {return scanStatus;}
}
