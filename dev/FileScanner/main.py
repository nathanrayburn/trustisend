import os

from flask import Flask
from enum import Enum
import google.cloud.firestore as firestore
import google.cloud.storage as storage

from activefile import ActiveFile

import json
import schedule


class FileScanStatus(Enum):
    PENDING = "PENDING"
    CLEAN = "CLEAN"
    INFECTED = "INFECTED"
    ERROR = "ERROR"

# Open and read the configuration.json file
with open('configuration.json') as config_file:
    data = json.load(config_file)

# Create Firestore client with its specific credentials
db = firestore.Client.from_service_account_json(data['firestore']['credentials'], project=data['projectID'], database=data['firestore']['databaseID'])
storage_client = storage.Client.from_service_account_json(data['storage']['credentials'])
bucketName = data['storage']['bucket']
app = Flask(__name__)

def getActiveFilesFromFirestore():
    activeFiles_ref = db.collection("files").where("scanStatus", "in",
                                                   [FileScanStatus.PENDING.value, FileScanStatus.ERROR.value])

    docs = activeFiles_ref.stream()
    activeFiles = []
    for doc in docs:
        activeFiles.append(ActiveFile(doc.to_dict()['groupUUID'], doc.id, doc.to_dict()['path'],
                                      doc.to_dict()['scanStatus']))
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

    tempDirectory = "temp/"
    # create the temp directory if it does not exist
    if not os.path.exists(tempDirectory):
        os.makedirs(tempDirectory)


    for file in files:
        bucket = storage_client.bucket(bucketName)
        blob = bucket.blob(file.group_uuid + "/" + file.path)
        blob.download_to_filename(tempDirectory + file.file_uuid + getExtension(file.path))
        print("Downloaded file: " + file.file_uuid)


def scanNewFiles():
    # get all files from firestore that have scanStatus = PENDING or ERROR
    activeFiles = getActiveFilesFromFirestore()
    for file in activeFiles:
        print(file.group_uuid, file.file_uuid, file.path, file.scan_status)
    # select the first n oldest files
    filesToScan = selectFirstOldestFiles(data['antivirus']['file-scan-limit'], activeFiles)
    for file in filesToScan:
        print(file.group_uuid, file.file_uuid, file.path, file.scan_status)

    # get the files from cloud storage
    downloadFilesFromCloudStorage(filesToScan)
    # scan and update the scanStatus in firestore

    # remove the files from the temp directory
    for file in filesToScan:
        os.remove("temp/" + file.file_uuid + getExtension(file.path))
    print("Function is running...")
scanNewFiles()
schedule.every(70).seconds.do(scanNewFiles)


@app.route('/')
def home():
    # Return the contents of the file as a string
    return str(data)

if __name__ == '__main__':
    app.run(debug=True)