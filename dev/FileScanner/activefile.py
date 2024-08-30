import group as Group

class ActiveFile():
    def __init__(self, group_uuid, file_uuid, path, scan_status):
        self.group_uuid = group_uuid
        self.file_uuid = file_uuid
        self.path = path
        self.scan_status = scan_status

    # Getter methods
    def get_file_uuid(self):
        return self.file_uuid

    def get_path(self):
        return self.path

    def get_scan_status(self):
        return self.scan_status