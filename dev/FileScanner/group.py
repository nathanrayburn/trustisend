from datetime import datetime

class InvalidTimestampFormatException(Exception):
    pass

class Group:
    def __init__(self, user_email, timestamp, number_downloads, group_uuid=None):
        self.group_uuid = group_uuid
        self.user_email = user_email
        self.timestamp = self.validate_and_format_timestamp(timestamp)
        self.number_downloads = number_downloads

    def get_group_uuid(self):
        return self.group_uuid

    def get_user_email(self):
        return self.user_email

    def get_timestamp(self):
        return self.timestamp

    def get_number_downloads(self):
        return self.number_downloads

    @staticmethod
    def validate_and_format_timestamp(timestamp):
        try:
            # Parse the input timestamp to validate and format it
            datetime_object = datetime.fromisoformat(timestamp)

            # Return the formatted timestamp as a string
            return datetime_object.isoformat()
        except ValueError:
            raise InvalidTimestampFormatException("Invalid timestamp format. Expected format: YYYY-MM-DDTHH:MM:SS")