package dev.test.trustisend.bean;

import dev.test.trustisend.bean.Group;

public class ActiveFile extends Group {
    private String path;

    public ActiveFile(String groupUUID, String userEmail, String directory, String timestamp, String numberDownloads, String path) {
        super(groupUUID, userEmail, directory, timestamp, numberDownloads);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
