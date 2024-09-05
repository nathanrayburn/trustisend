import unittest
from unittest.mock import patch, MagicMock
from datetime import datetime, timedelta, timezone
from main import delete_inactive_groups, delete_group_and_files


class TestDeleteGroups(unittest.TestCase):
    
    @patch('main.db')  # Mock Firestore
    @patch('main.storage_client')  # Mock Cloud Storage
    def test_delete_inactive_groups(self, mock_storage_client, mock_db):
        # Mock Firestore group data with lastDownloaded older than 30 days
        mock_group_stream = MagicMock()
        mock_group_doc = MagicMock()
        mock_group_doc.id = "group1"
        mock_group_doc.to_dict.return_value = {
            'lastDownloaded': MagicMock(to_datetime=lambda: datetime.now(timezone.utc) - timedelta(days=40)),
            'numberDownloads': 5
        }
        mock_group_stream.stream.return_value = [mock_group_doc]
        mock_db.collection.return_value = mock_group_stream

        # Mock Firestore file data
        mock_file_stream = MagicMock()
        mock_file_doc = MagicMock()
        mock_file_doc.id = "file1"
        mock_file_doc.to_dict.return_value = {'path': 'test_file.txt', 'groupUUID': 'group1'}
        mock_file_stream.stream.return_value = [mock_file_doc]

        # Mock file and group deletion
        mock_bucket = MagicMock()
        mock_storage_client.bucket.return_value = mock_bucket
        mock_blob = MagicMock()
        mock_bucket.blob.return_value = mock_blob

        # Mock Firestore delete calls
        mock_db.collection.return_value.document.return_value.delete = MagicMock()

        # Call the function under test
        delete_inactive_groups()

        # Verify that Firestore's delete function was called for both files and groups
        mock_db.collection.return_value.document.return_value.delete.assert_called()

        # Ensure the blob delete method was called
        mock_bucket.blob.assert_called_with("group1/test_file.txt")  # Ensure blob is fetched with the correct path
        mock_blob.delete.assert_called()  # Check that the Cloud Storage blob was deleted

    @patch('main.db')  # Mock Firestore
    @patch('main.storage_client')  # Mock Cloud Storage
    def test_delete_group_and_files(self, mock_storage_client, mock_db):
        # Mock Firestore file data for a group
        mock_file_stream = MagicMock()
        mock_file_doc = MagicMock()
        mock_file_doc.id = "file1"
        mock_file_doc.to_dict.return_value = {'path': 'test_file.txt', 'groupUUID': 'group1'}
        mock_file_stream.stream.return_value = [mock_file_doc]
        mock_db.collection.return_value.where.return_value.stream.return_value = [mock_file_doc]

        # Mock file deletion from Cloud Storage
        mock_bucket = MagicMock()
        mock_storage_client.bucket.return_value = mock_bucket
        mock_blob = MagicMock()
        mock_bucket.blob.return_value = mock_blob

        # Call the function under test
        delete_group_and_files("group1")

        # Verify that Firestore's delete function was called for the files and the group
        mock_db.collection.return_value.document.return_value.delete.assert_called()
        mock_blob.delete.assert_called()  # Verify that Cloud Storage's blob delete was called


if __name__ == '__main__':
    unittest.main()
