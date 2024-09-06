import unittest
from unittest.mock import patch, MagicMock
from datetime import datetime, timedelta, timezone
from main import track_download_activity, delete_inactive_groups, track_all_groups


class TestDownloadTracking(unittest.TestCase):

    @patch('main.db')  # Mock Firestore
    def test_track_download_activity_updates(self, mock_db):
        """
        Test that track_download_activity correctly updates the lastDownloaded if the download count increases by x.
        """
        # Mock the current group document with nbDownloaded = 10 (as an integer)
        mock_group_doc = MagicMock()
        mock_group_doc.exists = True
        mock_group_doc.to_dict.return_value = {'nbDownloaded': 10}

        # Mock the previous download activity document with nbDownloaded = 5 (as an integer)
        mock_download_doc = MagicMock()
        mock_download_doc.exists = True
        mock_download_doc.to_dict.return_value = {'nbDownloaded': 5}

        # Use side_effect to simulate different return values for multiple calls to get()
        mock_db.collection.return_value.document.return_value.get.side_effect = [mock_group_doc, mock_download_doc]

        # Call the function under test
        track_download_activity("group1")

        # Ensure the lastDownloaded timestamp is updated since the nbDownloaded increased by more than x_threshold
        mock_db.collection.return_value.document.return_value.set.assert_called()

    @patch('main.db')  # Mock Firestore
    def test_track_download_activity_no_update(self, mock_db):
        """
        Test that track_download_activity does not update the lastDownloaded if the download count doesn't increase by x.
        """
        # Mock the current group document with nbDownloaded = 6 (as an integer)
        mock_group_doc = MagicMock()
        mock_group_doc.exists = True
        mock_group_doc.to_dict.return_value = {'nbDownloaded': 6}

        # Mock the previous download activity document with nbDownloaded = 5 (as an integer)
        mock_download_doc = MagicMock()
        mock_download_doc.exists = True
        mock_download_doc.to_dict.return_value = {'nbDownloaded': 5}

        # Use side_effect to simulate different return values for multiple calls to get()
        mock_db.collection.return_value.document.return_value.get.side_effect = [mock_group_doc, mock_download_doc]

        # Call the function under test
        track_download_activity("group1")

        # Ensure the lastDownloaded timestamp is NOT updated since the nbDownloaded did not increase by x_threshold
        mock_db.collection.return_value.document.return_value.set.assert_not_called()

    @patch('main.db')  # Mock Firestore
    def test_delete_inactive_groups(self, mock_db):
        """
        Test that delete_inactive_groups deletes groups with lastDownloaded older than 30 days.
        """
        # Mock the group download data with lastDownloaded older than 30 days
        mock_download_stream = MagicMock()
        mock_download_doc = MagicMock()
        mock_download_doc.id = "group1"
        mock_download_doc.to_dict.return_value = {
            'lastDownloaded': MagicMock(to_datetime=lambda: datetime.now(timezone.utc) - timedelta(days=40))
        }
        mock_download_stream.stream.return_value = [mock_download_doc]
        mock_db.collection.return_value = mock_download_stream

        # Call the function under test
        delete_inactive_groups()

        # Verify that Firestore delete functions are called
        mock_db.collection.return_value.document.return_value.delete.assert_called()

    @patch('main.db')  # Mock Firestore
    def test_track_all_groups(self, mock_db):
        """
        Test that track_all_groups tracks the download activity for all groups.
        """
        # Mock group data
        mock_group_stream = MagicMock()
        mock_group_doc = MagicMock()
        mock_group_doc.id = "group1"
        mock_group_stream.stream.return_value = [mock_group_doc]
        mock_db.collection.return_value = mock_group_stream

        # Call the function under test
        track_all_groups()

        # Verify that the track_download_activity is called for each group
        mock_db.collection.return_value.document.return_value.get.assert_called()


if __name__ == '__main__':
    unittest.main()
