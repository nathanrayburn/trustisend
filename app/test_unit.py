import unittest
from unittest.mock import patch, MagicMock
from main import (  
    getActiveFilesFromFirestore,
    getTimestamp,
    selectFirstOldestFiles,
    getExtension,
    downloadFilesFromCloudStorage,
    isExpectedResults,
    FileScanStatus,
)

# Mock ActiveFile class since it's imported from another module
class MockActiveFile:
    def __init__(self, group_uuid, file_uuid, path, scan_status):
        self.group_uuid = group_uuid
        self.file_uuid = file_uuid
        self.path = path
        self.scan_status = scan_status

class TestUnit(unittest.TestCase):
    # test for pending or error status
    @patch('main.db')
    def test_getActiveFilesFromFirestore(self, mock_db):
        # Setup
        mock_collection = MagicMock()
        mock_db.collection.return_value = mock_collection
        mock_stream = MagicMock()
        mock_collection.where.return_value.stream.return_value = [mock_stream]

        mock_stream.to_dict.return_value = {
            'groupUUID': 'group1',
            'path': 'path/to/file',
            'scanStatus': FileScanStatus.PENDING.value
        }
        mock_stream.id = 'file1'

        # Execute
        result = getActiveFilesFromFirestore()

        # Verify
        self.assertEqual(result[0].group_uuid, 'group1')
        self.assertEqual(result[0].file_uuid, 'file1')
        self.assertEqual(result[0].path, 'path/to/file')
        self.assertEqual(result[0].scan_status, FileScanStatus.PENDING.value)

    @patch('main.db')
    def test_getTimestamp(self, mock_db):
        mock_doc = MagicMock()
        mock_doc.to_dict.return_value = {'timestamp': 1234567890}
        mock_db.collection().document().get.return_value = mock_doc

        timestamp = getTimestamp('group1')
        self.assertEqual(timestamp, 1234567890)

    def test_selectFirstOldestFiles(self):
        files = [
            MockActiveFile('group1', 'file1', 'path/to/file1', FileScanStatus.PENDING.value),
            MockActiveFile('group2', 'file2', 'path/to/file2', FileScanStatus.PENDING.value)
        ]

        with patch('main.getTimestamp', side_effect=lambda x: 100 if x == 'group1' else 200):
            selected_files = selectFirstOldestFiles(1, files)
            self.assertEqual(len(selected_files), 1)
            self.assertEqual(selected_files[0].file_uuid, 'file1')

    def test_getExtension(self):
        path = 'file.txt'
        extension = getExtension(path)
        self.assertEqual(extension, 'txt')

    @patch('main.storage_client')
    def test_downloadFilesFromCloudStorage(self, mock_storage_client):
        files = [
            MockActiveFile('group1', 'file1', 'path/to/file1.txt', FileScanStatus.PENDING.value)
        ]

        mock_bucket = MagicMock()
        mock_blob = MagicMock()
        mock_storage_client.bucket.return_value = mock_bucket
        mock_bucket.blob.return_value = mock_blob

        downloadFilesFromCloudStorage(files)

        mock_blob.download_to_filename.assert_called_with('temp/file1.txt')

    def test_isExpectedResults(self):
        self.assertEqual(isExpectedResults("malicious", 1), FileScanStatus.INFECTED)
        self.assertEqual(isExpectedResults("suspicious", 1), FileScanStatus.INFECTED)
        self.assertEqual(isExpectedResults("undetected", 1), FileScanStatus.CLEAN)
        self.assertEqual(isExpectedResults("timeout", 1), FileScanStatus.ERROR)
        self.assertEqual(isExpectedResults("confirmed-timeout", 1), FileScanStatus.ERROR)
        self.assertEqual(isExpectedResults("failure", 1), FileScanStatus.ERROR)
        self.assertEqual(isExpectedResults("harmless", 0), FileScanStatus.CLEAN)

if __name__ == '__main__':
    unittest.main()
