package dev.test.trustisend.util;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import dev.test.trustisend.entity.ActiveFile;
import dev.test.trustisend.entity.FileScanStatus;
import dev.test.trustisend.entity.Group;
import dev.test.trustisend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import com.google.cloud.firestore.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Component
public class FirestoreUtil {
    @Autowired
    private final Firestore firestore;

    @Autowired
    public FirestoreUtil(Firestore firestore) {
        this.firestore = firestore;
    }

    private static Map<String, Object> prepareUserData(dev.test.trustisend.entity.User user){
        return Map.of(
                "email", user.getEmail(),
                "hash", user.getHash()
        );
    }

    private static Map<String, Object> prepareActiveFileData(ActiveFile activeFile){
        return Map.of(
                "groupUUID", activeFile.getGroupUUID(),
                "path", activeFile.getPath(),
                "scanStatus", activeFile.getScanStatus().toString()
        );
    }

    /**
     * create a user in the db
     * @param user the user to be added
     * @return user that was added or null if user already exists
     */
    public User createUser(User user) throws Exception {
        // Check if the user already exists
        UserDetails existingUser = readUserByEmail(user.getEmail());
        if (existingUser != null) {
            System.out.println("User with email: " + user.getEmail() + " already exists");
            return null;
        }
        Map<String, Object> userData = prepareUserData(user);
        CollectionReference users = firestore.collection("users");
        ApiFuture<DocumentReference> result = users.add(userData);
        String userId = result.get().getId();
        System.out.println("User created with ID: " + userId);
        return new User(
                userId,
                user.getEmail(),
                user.getHash()
        );
    }

    /**
     * get a user from the db given his email
     * @param email of the user
     * @return user if he exists in the db
     * @throws Exception if the user doesn't exist in the db
     */
    public User readUserByEmail(String email) throws Exception {
        try {
            System.out.println("Looking for user with email: " + email);
            Query query = firestore.collection("users").whereEqualTo("email", email);
            ApiFuture<QuerySnapshot> future = query.get();
            QuerySnapshot querySnapshot = future.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            // Return null if no user is found
            if (documents.isEmpty()) {
                System.out.println("No user found with email: " + email);
                return null;
            }

            DocumentSnapshot document = documents.get(0);

            // Create and return the User object
            return new User(
                    document.getId(),
                    document.getString("email"),
                    document.getString("hash")
            );
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new Exception("Error fetching user data", e);
        }
    }

    /**
     * delete the user from the db
     * @param userId id of the user to delete
     */
    public void deleteUser(String userId) throws Exception {
        try {
            System.out.println("Attempting to delete user with ID: " + userId);

            // Retrieve the document reference for the given user ID
            DocumentReference docRef = firestore.collection("users").document(userId);

            // Execute the delete operation
            ApiFuture<WriteResult> writeResult = docRef.delete();

            // Wait for the delete operation to complete and get the result
            WriteResult result = writeResult.get();

            System.out.println("User with ID: " + userId + " deleted at: " + result.getUpdateTime());

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new Exception("Error deleting user data", e);
        }
    }

    /**
     * Create a new group and return the Group object. A group is all the files that are part of the same upload and
     * share the same download link
     * @param userEmail email of the user
     * @return group with id
     */
    public Group createGroup(String userEmail) throws Exception {
        Map<String, Object> groupData = Map.of(
                "userEmail", userEmail,
                "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "numberDownloads", 0
        );


        try {
            CollectionReference groups = firestore.collection("groups");
            ApiFuture<DocumentReference> result = groups.add(groupData);
            String groupId = result.get().getId();
            System.out.println("Group created with ID: " + groupId);

            // Construct the Group object using the returned group ID and input data
            return new Group(
                    groupId,
                    (String) groupData.get("userEmail"),
                    (String) groupData.get("timestamp"),
                    (Integer) groupData.get("numberDownloads")
            );

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new Exception("Error creating group", e);
        }
    }

    /**
     * get a group from the db
     * @param groupUUID uuid of the group
     * @return Group or null if the group doesn't exist
     * @throws Exception
     */
    public Group readGroupByUUID(String groupUUID) throws Exception {
        try {
            System.out.println("Looking for group with ID: " + groupUUID);

            // Retrieve the document reference for the given group ID
            DocumentReference docRef = firestore.collection("groups").document(groupUUID);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            // Check if the document exists
            if (!document.exists()) {
                System.out.println("No group found with ID: " + groupUUID);
                return null;
            }

            // Create and return the Group object
            return new Group(
                    groupUUID,
                    document.getString("userEmail"),
                    document.getString("timestamp"),
                    document.getLong("numberDownloads").intValue()
            );

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new Exception("Error fetching group data", e);
        }
    }

    /**
     * deletes a group from the db
     * @param groupId id of the group
     * @throws Exception
     */
    public void deleteGroup(String groupId) throws Exception {
        try {
            System.out.println("Attempting to delete group with ID: " + groupId);

            // Retrieve the document reference for the given group ID
            DocumentReference docRef = firestore.collection("groups").document(groupId);

            // Execute the delete operation
            ApiFuture<WriteResult> writeResult = docRef.delete();

            // Wait for the delete operation to complete and get the result
            WriteResult result = writeResult.get();

            System.out.println("Group with ID: " + groupId + " deleted at: " + result.getUpdateTime());

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new Exception("Error deleting group data", e);
        }
    }

    /**
     * increments the download count of a group
     * @param groupUUID id of the group
     * @throws Exception
     */
    public void incrementDownloadCount(String groupUUID) throws Exception {
        try {
            System.out.println("Attempting to increment download count for group with ID: " + groupUUID);

            // Retrieve the document reference for the given group ID
            DocumentReference docRef = firestore.collection("groups").document(groupUUID);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            // Check if the document exists
            if (!document.exists()) {
                System.out.println("No group found with ID: " + groupUUID);
                return;
            }

            // Increment the download count
            int currentDownloads = document.getLong("numberDownloads").intValue();
            int newDownloads = currentDownloads + 1;

            // Update the download count in Firestore
            ApiFuture<WriteResult> writeResult = docRef.update("numberDownloads", newDownloads);
            WriteResult result = writeResult.get();

            System.out.println("Download count for group with ID: " + groupUUID + " incremented to: " + newDownloads);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new Exception("Error incrementing download count", e);
        }
    }

    /**
     * deletes a group and all files that are part of the group from the db
     * @param groupUUID id of the group
     * @throws Exception
     */
    public void deleteGroupWithDependecies(String groupUUID) throws Exception {
        try {
            System.out.println("Attempting to delete group with ID: " + groupUUID);
            // retrieve all files which contain the groupUUID
            LinkedList<ActiveFile> activeFiles = readActiveFilesByGroupUUID(groupUUID);
            // for each file delete the file
            for(ActiveFile activeFile : activeFiles){
                deleteActiveFile(activeFile.getFileUUID());
            }
            // delete the group
            deleteGroup(groupUUID);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new Exception("Error deleting group data", e);
        }

    }

    /**
     * get the info about a file from the db
     * @param activeFileId id of the file
     * @return ActiveFile object that contains the file path and scan status of the file in gcloud
     * @throws Exception
     */
    public ActiveFile readActiveFileByUUID(String activeFileId) throws Exception {
        try {
            System.out.println("Looking for active file with ID: " + activeFileId);

            // Retrieve the document reference for the given activeFile ID
            DocumentReference docRef = firestore.collection("files").document(activeFileId);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            // Check if the document exists
            if (!document.exists()) {
                System.out.println("No active file found with ID: " + activeFileId);
                return null;
            }
            Group group = readGroupByUUID(document.getString("groupUUID"));
            // Create and return the ActiveFile object
            return new ActiveFile(
                    group,
                    activeFileId,
                    document.getString("path"),
                    FileScanStatus.valueOf(document.getString("scanStatus"))
            );

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new Exception("Error fetching active file data", e);
        }
    }

    /**
     * get all groups that belong to a user
     * @param email email address of the user
     * @return list of the user's groups
     * @throws Exception
     */
    public LinkedList<Group> readGroupsByEmail(String email) throws Exception{
        try {


            Query query = firestore.collection("groups").whereEqualTo("userEmail", email);
            ApiFuture<QuerySnapshot> future = query.get();
            QuerySnapshot querySnapshot = future.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            if (documents.isEmpty()) {
                System.out.println("No group found with email: " + email);
                return null;
            }
            LinkedList<Group> groups = new LinkedList<Group>();
            for (DocumentSnapshot doc : documents) {
                groups.add(new Group(
                        doc.getId(),
                        doc.getString("userEmail"),
                        doc.getString("timestamp"),
                        doc.getLong("numberDownloads").intValue()
                ));
            }
            return groups;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new Exception("Error fetching group data", e);
        }
    }

    /**
     * get the info of all files in a group
     * @param groupUUID id of the group
     * @return a list of ActiveFile objects that contain info about the files in gcloud
     * @throws Exception
     */
    public LinkedList<ActiveFile> readActiveFilesByGroupUUID(String groupUUID) throws Exception {
        try {
            System.out.println("Looking for active files with group ID: " + groupUUID);

            Query query = firestore.collection("files").whereEqualTo("groupUUID", groupUUID);
            ApiFuture<QuerySnapshot> future = query.get();
            QuerySnapshot querySnapshot = future.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            if (documents.isEmpty()) {
                System.out.println("No file found with groupUUID: " + groupUUID);
                return null;
            }
            DocumentSnapshot document = documents.get(0);

            // Check if the document exists
            if (!document.exists()) {
                System.out.println("No active files found with group ID: " + groupUUID);
                return null;
            }

            LinkedList<ActiveFile> activeFiles = new LinkedList<ActiveFile>();

            // for each Document create active file and add to list
            Group group = readGroupByUUID(groupUUID);
            for (DocumentSnapshot doc : documents) {
                activeFiles.add(new ActiveFile(
                    group,
                    doc.getId(),
                    doc.getString("path"),
                    FileScanStatus.valueOf(doc.getString("scanStatus"))

                ));
            }
            return activeFiles;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new Exception("Error fetching group data", e);
        }
    }

    /**
     * create an ActiveFile entry in the db
     * @param activeFile
     * @return the ActiveFile with an ID
     * @throws Exception
     */
    public ActiveFile createActiveFile(ActiveFile activeFile) throws Exception {
        Map<String, Object> activeFileData = prepareActiveFileData(activeFile);
        try {
            CollectionReference activeFiles = firestore.collection("files");
            ApiFuture<DocumentReference> result = activeFiles.add(activeFileData);
            String activeFileId = result.get().getId();
            System.out.println("ActiveFile created with ID: " + activeFileId);

            // Construct the ActiveFile object using the returned activeFile ID and input data

            return new ActiveFile(
                    activeFile.getGroupUUID(),
                    activeFile.getUserEmail(),
                    activeFile.getTimestamp(),
                    activeFile.getNumberDownloads(),
                    activeFileId,
                    (String) activeFileData.get("path"),
                    FileScanStatus.valueOf((String) activeFileData.get("scanStatus"))
            );

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new Exception("Error creating activeFile", e);
        }
    }

    /**
     * delete the entry of a file in the db
     * @param activeFileId
     * @throws Exception
     */
    public void deleteActiveFile(String activeFileId) throws Exception {
        try {
            System.out.println("Attempting to delete activeFile with ID: " + activeFileId);

            // Retrieve the document reference for the given activeFile ID
            DocumentReference docRef = firestore.collection("files").document(activeFileId);

            // Execute the delete operation
            ApiFuture<WriteResult> writeResult = docRef.delete();

            // Wait for the delete operation to complete and get the result
            WriteResult result = writeResult.get();

            System.out.println("ActiveFile with ID: " + activeFileId + " deleted at: " + result.getUpdateTime());

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new Exception("Error deleting activeFile data", e);
        }
    }
}
