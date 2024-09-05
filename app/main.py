import os
import google.cloud.firestore as firestore
import google.cloud.storage as storage
from datetime import datetime, timedelta, timezone
import logging
import json
import schedule
import time

# Load configuration from the configuration.json file
with open('configuration.json') as config_file:
    config = json.load(config_file)

# Configuration values
bucket_name = config['storage']['bucket']
thirty_days = timedelta(days=30)

# Initialize Firestore and Cloud Storage clients using the paths from configuration.json
db = firestore.Client.from_service_account_json(config['firestore']['credentials'], 
                                                project=config['projectID'],
                                                database=config['firestore']['databaseID'])

storage_client = storage.Client.from_service_account_json(config['storage']['credentials'])


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

    except Exception as e:
        logging.error(f"An error occurred while deleting group and files: {e}")
        import traceback
        traceback.print_exc()


def delete_inactive_groups():
    """
    Function to check each group and delete it along with its associated files if it hasn't been downloaded for more than 30 days.
    """
    try:
        # Get the current time
        now = datetime.now(timezone.utc)

        # Define the cutoff date for groups not downloaded in the last 30 days
        cutoff_date = now - thirty_days

        # Query Firestore for all groups
        all_groups_ref = db.collection("groups")
        groups = all_groups_ref.stream()

        for group_doc in groups:
            group_data = group_doc.to_dict()

            # Check if the 'lastDownloaded' field exists in the group document
            if 'lastDownloaded' in group_data:
                last_downloaded = group_data['lastDownloaded']
                last_downloaded_datetime = last_downloaded.to_datetime()

                # If the group hasn't been downloaded for more than 30 days, delete the group and its files
                if last_downloaded_datetime < cutoff_date:
                    group_uuid = group_doc.id
                    print(f"Group {group_uuid} has not been downloaded for 30 days, deleting...")
                    delete_group_and_files(group_uuid)

    except Exception as e:
        logging.error(f"An error occurred while checking for inactive groups: {e}")
        import traceback
        traceback.print_exc()


def schedule_cleanup():
    """
    Function to schedule the deletion of inactive groups and their associated files every day at midnight.
    """
    # Schedule the delete_inactive_groups function to run once a day at midnight
    schedule.every().day.at("00:00").do(delete_inactive_groups)
    
    # Keep the scheduler running indefinitely
    while True:
        schedule.run_pending()
        time.sleep(60)  # Sleep for 1 minute between checks
