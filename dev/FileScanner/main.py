import os
from flask import Flask
from enum import Enum
import google.cloud.firestore as firestore
import google.cloud.storage as storage
import requests
from activefile import ActiveFile
import json
import schedule

app = Flask(__name__)

tempDirectory = "temp/"
url = "https://www.virustotal.com/api/v3/files"


class FileScanStatus(Enum):
    PENDING = "PENDING"
    CLEAN = "CLEAN"
    INFECTED = "INFECTED"
    ERROR = "ERROR"


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
    docs = activeFiles_ref.stream()
    activeFiles = []
    for doc in docs:
        activeFiles.append(
            ActiveFile(doc.to_dict()['groupUUID'], doc.id, doc.to_dict()['path'], doc.to_dict()['scanStatus']))
    return activeFiles


def getTimestamp(group_uuid):
    group_ref = db.collection("groups").document(group_uuid)
    group = group_ref.get()
    return group.to_dict()['timestamp']


def selectFirstOldestFiles(numberOfFiles, activeFiles):
    if len(activeFiles) == 0:
        return []
    if len(activeFiles) <= numberOfFiles:
        return activeFiles
    activeFiles.sort(key=lambda x: getTimestamp(x.group_uuid))
    return activeFiles[:numberOfFiles]


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
    files = {"file": (fileName, open(fileName, "rb"), "application/octet-stream")}
    key = readAPIKey()
    headers = {
        "accept": "application/json",
        "x-apikey": key
    }

    response = requests.post(url, files=files, headers=headers)

    print(response.text)
    return response


# hashmap to track the analysis id of each file
hashmap = {}


def associateFileToAnalysisID(file, data):
    # add to hashmap
    hashmap[file.file_uuid] = data


def isExpectedResults(key, value):
    '''
                "malicious": x,
                "suspicious": x,
                "undetected": x,
                "harmless": x,
                "timeout": x,
                "confirmed-timeout": x,
                "failure": x,
                "type-unsupported": x
    '''
    if key == "malicious" and value > 0:
        print("File is malicious")
        return FileScanStatus.INFECTED
    elif key == "suspicious" and value > 0:
        print("File is suspicious")
        return FileScanStatus.INFECTED
    elif key == "undetected" and value > 0:
        print("File is undetected")
        return FileScanStatus.CLEAN
    elif key == "timeout" and value > 0:
        print("File is timeout")
        return FileScanStatus.ERROR
    elif key == "confirmed-timeout" and value > 0:
        print("File is confirmed-timeout")
        return FileScanStatus.ERROR
    elif key == "failure" and value > 0:
        print("File is failure")
        return FileScanStatus.ERROR
    print("File is clean")
    return FileScanStatus.CLEAN


def followUpFileAnalysis():
    keys_to_remove = []  # List to keep track of keys to remove

    for file_uuid, data in hashmap.items():
        if handleFile(file_uuid, data):
            keys_to_remove.append(file_uuid)  # Mark for removal
            continue  # Move to the next item if handleFile returns True

    # Remove the keys after the iteration is complete
    for key in keys_to_remove:
        hashmap.pop(key)


def handleFile(file_uuid, data):
    analysis_id = data['id']
    url = data['links']['self']
    key = readAPIKey()
    headers = {
        "accept": "application/json",
        "x-apikey": key
    }
    response = requests.get(url, headers=headers)
    if response.status_code == 200:
        print(response.text)
        res = response.json()
        if res['data']['attributes']['status'] == "completed":
            status = FileScanStatus.PENDING

            for key, value in res['data']['attributes']['stats'].items():
                status = isExpectedResults(key, value)
                if status == FileScanStatus.INFECTED or status == FileScanStatus.ERROR:
                    db.collection("files").document(file_uuid).update({"scanStatus": status.value})
                    return True  # Early return to continue outer loop

            # update firestore
            db.collection("files").document(file_uuid).update({"scanStatus": status.value})
            print("File " + file_uuid + " is clean")
        elif res['data']['attributes']['status'] == "queued":
            print("File " + file_uuid + " is queued")
        elif res['data']['attributes']['status'] == "in-progress":
            print("File " + file_uuid + " is in progress")
    else:
        print("Error in follow up file analysis")
        print(response.text)
        db.collection("files").document(file_uuid).update({"scanStatus": FileScanStatus.ERROR.value})
        return True  # Mark for removal since an error occurred
    return False  # Do not mark for removal


def scanNewFiles():
    # get all files from firestore that have scanStatus = PENDING
    activeFiles = getActiveFilesFromFirestore()

    if not activeFiles:
        print("No files found with PENDING status.")
        return  # Return early if no files to process

    for file in activeFiles:
        print(file.group_uuid, file.file_uuid, file.path, file.scan_status)

    # select the first n oldest files
    filesToScan = selectFirstOldestFiles(data['antivirus']['file-scan-limit'], activeFiles)
    for file in filesToScan:
        print(file.group_uuid, file.file_uuid, file.path, file.scan_status)

    # get the files from cloud storage
    downloadFilesFromCloudStorage(filesToScan)

    # scan and update the scanStatus in firestore
    for file in filesToScan:
        res = sendToAPI(file)
        if res.status_code == 200:
            associateFileToAnalysisID(file, res.json()['data'])
        else:
            db.collection("files").document(file.file_uuid).update({"scanStatus": FileScanStatus.ERROR.value})

    # remove the files from the temp directory
    for file in filesToScan:
        try:
            os.remove(tempDirectory + file.file_uuid + "." + getExtension(file.path))
        except OSError as e:
            print(f"Error deleting file {file.file_uuid}: {e}")

    followUpFileAnalysis()


#scanNewFiles()


# schedule.every(70).seconds.do(scanNewFiles)

@app.route('/')
def home():
    # Return the contents of the file as a string
    return str("API is running")


if __name__ == '__main__':
    app.run(debug=True)
