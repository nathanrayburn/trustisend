import os
from flask import Flask
from enum import Enum
import google.cloud.firestore as firestore
import google.cloud.storage as storage
import requests
from requests.adapters import HTTPAdapter
from urllib3.poolmanager import PoolManager
import ssl
from activefile import ActiveFile
import json
import logging
import subprocess
import sys
import schedule
from waitress import serve
import threading  # Import threading
import time  # Import time for the scheduler loop

app = Flask(__name__)

tempDirectory = "temp/"
url = "https://www.virustotal.com/api/v3/files"
upload_url_endpoint = "https://www.virustotal.com/api/v3/files/upload_url"

class FileScanStatus(Enum):
    PENDING = "PENDING"
    CLEAN = "CLEAN"
    INFECTED = "INFECTED"
    ERROR = "ERROR"


# SSL Adapter class to enforce TLS 1.2 or higher
class SSLAdapter(HTTPAdapter):
    def init_poolmanager(self, *args, **kwargs):
        context = ssl.create_default_context()
        kwargs['ssl_context'] = context
        return super().init_poolmanager(*args, **kwargs)

# Create a session and mount the SSLAdapter
session = requests.Session()
session.mount('https://', SSLAdapter())

# Open and read the configuration.json file
with open('configuration.json') as config_file:
    data = json.load(config_file)

# Create Firestore client with its specific credentials
db = firestore.Client.from_service_account_json(data['firestore']['credentials'], project=data['projectID'],
                                                database=data['firestore']['databaseID'])
storage_client = storage.Client.from_service_account_json(data['storage']['credentials'])
bucketName = data['storage']['bucket']


def getActiveFilesFromFirestore():
    activeFiles_ref = db.collection("files").where("scanStatus", "in", [FileScanStatus.PENDING.value])
    errorFiles_ref = db.collection("files").where("scanStatus", "==", FileScanStatus.ERROR.value)
    active = activeFiles_ref.stream()
    error = errorFiles_ref.stream()

    activeFiles = []
    for doc in active:
        activeFiles.append(
            ActiveFile(doc.to_dict()['groupUUID'], doc.id, doc.to_dict()['path'], doc.to_dict()['scanStatus']))
    for doc in error:
        activeFiles.append(
            ActiveFile(doc.to_dict()['groupUUID'], doc.id, doc.to_dict()['path'], doc.to_dict()['scanStatus']))
    return activeFiles


def getTimestamp(group_uuid):
    group_ref = db.collection("groups").document(group_uuid)
    group = group_ref.get()
    if group.to_dict() is None:
        return 0
    return group.to_dict()['timestamp']


def selectFirstOldestFiles(numberOfFiles, activeFiles):
    if len(activeFiles) == 0:
        return []
    if len(activeFiles) <= numberOfFiles:
        return activeFiles
    
    files_with_timestamps = [(file, getTimestamp(file.group_uuid)) for file in activeFiles]

    valid_files = [file for file, timestamp in files_with_timestamps if timestamp != 0]

    valid_files.sort(key=lambda x: getTimestamp(x.group_uuid))
    return valid_files[:numberOfFiles]


def getExtension(path):
    return path.split('.')[-1]


def downloadFilesFromCloudStorage(files):
    # create the temp directory if it does not exist
    if not os.path.exists(tempDirectory):
        os.makedirs(tempDirectory)

    for file in files:
        try:
            bucket = storage_client.bucket(bucketName)
            blob = bucket.blob(file.group_uuid + "/" + file.path)
            blob.download_to_filename(tempDirectory + file.file_uuid + "." + getExtension(file.path))
            print("Downloaded file: " + file.file_uuid)
        except Exception as e:
            print(f"Error downloading file {file.file_uuid}: {e}")
            db.collection("files").document(file.file_uuid).update({"scanStatus": FileScanStatus.ERROR.value})
            continue  # Skip to the next file


def getFileIntoPayload(file):
    return {
        'file': open(tempDirectory + file.file_uuid + "." + getExtension(file.path), 'rb')
    }


def readAPIKey():
    with open(data["antivirus"]["api-key"], 'r') as apikey_file:
        return apikey_file.readline()


def sendToAPI(file):
    fileName = tempDirectory + file.file_uuid + "." + getExtension(file.path)
    file_size = os.path.getsize(fileName)
    key = readAPIKey()
    headers = {
        "accept": "application/json",
        "x-apikey": key
    }

    # If the file is larger than 32MB, use the upload URL method
    if file_size > 32 * 1024 * 1024:  # 32MB in bytes
        response = session.get(upload_url_endpoint, headers=headers)
        if response.status_code == 200:
            upload_url = response.json()["data"]
            with open(fileName, "rb") as f:
                files = {"file": (fileName, f, "application/octet-stream")}
                upload_response = session.post(upload_url, files=files, headers=headers)
                return upload_response
        else:
            print(f"Failed to get upload URL: {response.text}")
            db.collection("files").document(file.file_uuid).update({"scanStatus": FileScanStatus.ERROR.value})
            return response
    else:
        # For files <= 32MB, use the standard upload method
        with open(fileName, "rb") as f:
            files = {"file": (fileName, f, "application/octet-stream")}
            response = session.post(url, files=files, headers=headers)
            return response


# hashmap to track the analysis id of each file
hashmap = {}


def associateFileToAnalysisID(file, data):
    hashmap[file.file_uuid] = data


def isExpectedResults(key, value):
    if key == "malicious" and value > 0:
        return FileScanStatus.INFECTED
    elif key == "suspicious" and value > 0:
        return FileScanStatus.INFECTED
    elif key == "undetected" and value > 0:
        return FileScanStatus.CLEAN
    elif key == "timeout" and value > 0:
        return FileScanStatus.ERROR
    elif key == "confirmed-timeout" and value > 0:
        return FileScanStatus.ERROR
    elif key == "failure" and value > 0:
        return FileScanStatus.ERROR
    return FileScanStatus.CLEAN


def followUpFileAnalysis():
    keys_to_remove = []  # List to keep track of keys to remove

    for file_uuid, data in hashmap.items():
        if handleFile(file_uuid, data):
            keys_to_remove.append(file_uuid)  # Mark for removal
            continue

    # Remove the keys after the iteration is complete
    for key in keys_to_remove:
        print(f"Removing file {key} from hashmap")
        hashmap.pop(key)
        print("Hashmap after removal: ", hashmap)


def handleFile(file_uuid, data):
    analysis_id = data['id'] 
    url = data['links']['self']
    key = readAPIKey()
    headers = {
        "accept": "application/json",
        "x-apikey": key
    }
    response = session.get(url, headers=headers)
    if response.status_code == 200:
        res = response.json()
        if res['data']['attributes']['status'] == "completed":
            status = FileScanStatus.PENDING
            for key, value in res['data']['attributes']['stats'].items():
                status = isExpectedResults(key, value)
                if status == FileScanStatus.INFECTED:
                    db.collection("files").document(file_uuid).update({"scanStatus": status.value})
                    return True

            # update firestore
            db.collection("files").document(file_uuid).update({"scanStatus": status.value})
            if status == FileScanStatus.CLEAN:
                print("File " + file_uuid + " is clean")
                return True
        elif res['data']['attributes']['status'] == "queued":
            print("File " + file_uuid + " is queued")
        elif res['data']['attributes']['status'] == "in-progress":
            print("File " + file_uuid + " is in progress")
    else:
        print("Error in follow-up file analysis")
        print(response.text)
        db.collection("files").document(file_uuid).update({"scanStatus": FileScanStatus.ERROR.value})
        return True
    return False


def scanNewFiles():
    try:
        # Get all files from Firestore that have scanStatus = PENDING
        activeFiles = getActiveFilesFromFirestore()
        if not activeFiles:
            logging.debug("No files found with PENDING status.")
            return

        # Remove files that are in the hashmap
        activeFiles = [file for file in activeFiles if file.file_uuid not in hashmap]
        if not activeFiles:
            logging.debug("No new files found after filtering by hashmap.")
            return

        # Select the first n oldest files
        filesToScan = selectFirstOldestFiles(data['antivirus']['file-scan-limit'], activeFiles)

        # Get the files from cloud storage
        downloadFilesFromCloudStorage(filesToScan)

        # Scan and update the scanStatus in Firestore
        for file in filesToScan:
            try:
                res = sendToAPI(file)
                if res.status_code == 200:
                    associateFileToAnalysisID(file, res.json()['data'])
                    logging.debug(f"File {file.file_uuid} sent to API successfully.")
                else:
                    db.collection("files").document(file.file_uuid).update({"scanStatus": FileScanStatus.ERROR.value})
                    logging.debug(f"File {file.file_uuid} failed API scan.")
            except FileNotFoundError as e:
                logging.debug(f"Error: {e}")
                db.collection("files").document(file.file_uuid).update({"scanStatus": FileScanStatus.ERROR.value})
            
        # Remove the files from the temp directory
        for file in filesToScan:
            try:
                os.remove(tempDirectory + file.file_uuid + "." + getExtension(file.path))
                logging.debug(f"File {file.file_uuid} deleted successfully.")
            except OSError as e:
                logging.debug(f"Error deleting file {file.file_uuid}: {e}")
                db.collection("files").document(file.file_uuid).update({"scanStatus": FileScanStatus.ERROR.value})

    except Exception as e:
        logging.debug(f"An error occurred while scanning files: {e}")
        import traceback
        traceback.print_exc()


def safe_execution(task_function):
    def wrapper():
        try:
            task_function()
        except Exception as e:
            print(f"An error occurred in scheduled task {task_function.__name__}: {e}")
            import traceback
            traceback.print_exc()
    return wrapper


# Schedule the scanNewFiles function to run every 2 minute using the safe_execution wrapper
schedule.every(60).seconds.do(safe_execution(scanNewFiles))
schedule.every(10).seconds.do(safe_execution(followUpFileAnalysis))

def run_scheduler():
    while True:
        schedule.run_pending()
        time.sleep(1)


@app.route('/')
def home():
    return str("API is running")


if __name__ == '__main__':
    logging.info("Running specific tests: test_unit.py and test_system.py")

    test_files = ['test_unit.py', 'test_system.py']

    result = subprocess.run(
        [sys.executable, '-m', 'pytest', '--junitxml=./test-results.xml'] + test_files
    )

    if result.returncode != 0:
        logging.error("Tests failed. Exiting...")
        sys.exit(result.returncode)
    
    # Start the scheduler thread
    scheduler_thread = threading.Thread(target=run_scheduler)
    scheduler_thread.daemon = True  # Daemonize thread to ensure it exits when main program exits
    scheduler_thread.start()

    # Start the Flask application
    serve(app, host="0.0.0.0", port=8080)