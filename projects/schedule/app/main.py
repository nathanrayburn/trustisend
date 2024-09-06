import os
import threading
import google.cloud.firestore as firestore
import google.cloud.storage as storage
from datetime import datetime, timedelta, timezone
import logging
import json
import schedule
import time
import subprocess
import sys
from flask import Flask
from waitress import serve

app = Flask(__name__)

@app.route('/')
def home():
    # Return the contents of the file as a string
    return str("API is running")

# Load configuration from the configuration.json file
with open('configuration.json') as config_file:
    config = json.load(config_file)

# Configuration values
bucket_name = config['storage']['bucket']
thirty_days = timedelta(days=30)
x_threshold = 5  # Update lastDownloaded only when nbDownloaded increases by 5

# Initialize Firestore and Cloud Storage clients using the paths from configuration.json
db = firestore.Client.from_service_account_json(config['firestore']['credentials'], 
                                                project=config['projectID'],
                                                database=config['firestore']['databaseID'])

storage_client = storage.Client.from_service_account_json(config['storage']['credentials'])


def track_download_activity(group_uuid):
    """
    Tracks download activity by updating the lastDownloaded timestamp only if the nbDownloaded has increased by x.
    """
    try:
        # Retrieve current nbDownloaded from the groups collection
        group_ref = db.collection("groups").document(group_uuid)
        group_doc = group_ref.get()
        if not group_doc.exists:
            logging.error(f"Group {group_uuid} not found.")
            return
        group_data = group_doc.to_dict()
        current_nb_downloaded = group_data.get('nbDownloaded', 0)

        # Retrieve the previous nbDownloaded and lastDownloaded from the groupDownloads collection
        download_ref = db.collection("groupDownloads").document(group_uuid)
        download_doc = download_ref.get()

        if download_doc.exists:
            download_data = download_doc.to_dict()
            last_nb_downloaded = download_data.get('nbDownloaded', 0)
        else:
            # If no previous record exists, initialize it
            last_nb_downloaded = 0

        # Update the lastDownloaded timestamp only if the nbDownloaded has increased by at least x
        if current_nb_downloaded - last_nb_downloaded >= x_threshold:
            download_ref.set({
                'lastDownloaded': datetime.now(timezone.utc),
                'nbDownloaded': current_nb_downloaded  # Update the nbDownloaded for future comparison
            }, merge=True)  # Merge so that it updates or creates the document
            print(f"Tracked download activity for group {group_uuid}, updated lastDownloaded.")
        else:
            print(f"Tracked download activity for group {group_uuid}, but no need to update lastDownloaded.")

    except Exception as e:
        logging.error(f"Error tracking download activity for group {group_uuid}: {e}")
        import traceback
        traceback.print_exc()


def delete_group_and_files(group_uuid):
    """
    Deletes a group and all its associated files from Firestore and Google Cloud Storage.
    """
    try:
        # Delete all associated files from Firestore and Cloud Storage
        files_ref = db.collection("files").where("groupUUID", "==", group_uuid)
        files = files_ref.stream()

        for file_doc in files:
            file_data = file_doc.to_dict()
            file_path = file_data.get('path')

            # Delete file from Cloud Storage
            try:
                bucket = storage_client.bucket(bucket_name)
                blob = bucket.blob(f"{group_uuid}/{file_path}")
                blob.delete()
                print(f"Deleted file from Cloud Storage: {file_path}")
            except Exception as e:
                logging.error(f"Error deleting file from Cloud Storage: {file_path}: {e}")
                continue

            # Delete the file document from Firestore
            try:
                db.collection("files").document(file_doc.id).delete()
                print(f"Deleted Firestore document for file: {file_doc.id}")
            except Exception as e:
                logging.error(f"Error deleting Firestore document for file {file_doc.id}: {e}")

        # After deleting all files, delete the group itself
        try:
            db.collection("groups").document(group_uuid).delete()
            print(f"Deleted Firestore document for group: {group_uuid}")
        except Exception as e:
            logging.error(f"Error deleting Firestore document for group {group_uuid}: {e}")

        # Delete the group from groupDownloads
        try:
            db.collection("groupDownloads").document(group_uuid).delete()
            print(f"Deleted Firestore document for download activity of group: {group_uuid}")
        except Exception as e:
            logging.error(f"Error deleting group download activity: {e}")

    except Exception as e:
        logging.error(f"An error occurred while deleting group and files: {e}")
        import traceback
        traceback.print_exc()


def delete_inactive_groups():
    """
    Function to check each group in groupDownloads and delete it along with its associated files
    if the lastDownloaded timestamp is older than 30 days.
    """
    try:
        # Get the current time
        now = datetime.now(timezone.utc)

        # Define the cutoff date for inactivity (30 days ago)
        cutoff_date = now - thirty_days

        # Query Firestore for all groups' download activities
        download_ref = db.collection("groupDownloads")
        download_activities = download_ref.stream()

        for download_doc in download_activities:
            download_data = download_doc.to_dict()

            # Check if the 'lastDownloaded' field exists in the download document
            if 'lastDownloaded' in download_data:
                last_downloaded = download_data['lastDownloaded'].to_datetime()

                # If the lastDownloaded timestamp is older than 30 days, delete the group and its files
                if last_downloaded < cutoff_date:
                    group_uuid = download_doc.id
                    print(f"Group {group_uuid} has been inactive for 30 days, deleting...")
                    delete_group_and_files(group_uuid)

    except Exception as e:
        logging.error(f"An error occurred while checking for inactive groups: {e}")
        import traceback
        traceback.print_exc()


def track_all_groups():
    """
    Tracks download activity for all groups in the Firestore database.
    This function is scheduled to run once a day.
    """
    try:
        # Query Firestore for all groups
        all_groups_ref = db.collection("groups")
        groups = all_groups_ref.stream()

        for group_doc in groups:
            group_uuid = group_doc.id
            track_download_activity(group_uuid)

    except Exception as e:
        logging.error(f"An error occurred while tracking all groups: {e}")
        import traceback
        traceback.print_exc()


def schedule_cleanup():
    """
    Function to schedule the deletion of inactive groups and their associated files every day at midnight,
    and track download activity once a day.
    """
    # Schedule the delete_inactive_groups function to run once a day at midnight
    schedule.every().day.at("00:03").do(delete_inactive_groups)

    # Schedule the track_all_groups function to run once a day (e.g., at 1:00 AM)
    schedule.every().day.at("00:02").do(track_all_groups)
    
    # Keep the scheduler running indefinitely
    while True:
        schedule.run_pending()
        time.sleep(1)  # Sleep for 1 minute between checks


def run_tests():
    """
    Function to run the unit tests before executing the main application.
    """
    result = subprocess.run([sys.executable, "-m", "unittest", "discover", "-s", ".", "-p", "test_unit.py"])
    if result.returncode != 0:
        print("Unit tests failed. Exiting...")
        sys.exit(result.returncode)


if __name__ == '__main__':
    # Run the tests before proceeding
    run_tests()
    # Start the cleanup and tracking scheduler
    print("Starting cleanup and download tracking scheduler...")
    scheduler_thread = threading.Thread(target=schedule_cleanup)
    scheduler_thread.daemon = True  # Daemonize thread to ensure it exits when main program exits
    scheduler_thread.start()
    # Start the Flask application
    serve(app, host="0.0.0.0", port=8080)
