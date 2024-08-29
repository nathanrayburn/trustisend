package dev.test.trustisend.util;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import com.google.cloud.firestore.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Component
public class FirestoreUtil {

    private final Firestore firestore;

    @Autowired
    public FirestoreUtil(Firestore firestore) {
        this.firestore = firestore;
    }
    public static Map<String, Object> prepareUserData(dev.test.trustisend.bean.User user){
        return Map.of(
                "email", user.getEmail(),
                "hash", user.getHash()
        );
    }
    public String createUser(Map<String, Object> userData) throws Exception {
        CollectionReference users = firestore.collection("users");
        ApiFuture<DocumentReference> result = users.add(userData);
        String userId = result.get().getId();
        System.out.println("User created with ID: " + userId);
        return userId;
    }

    public UserDetails readUserByEmail(String email) throws Exception{
        try{
            System.out.println("Looking for user with email: " + email);
            Query query = firestore.collection("users").whereEqualTo("email", email);
            ApiFuture<QuerySnapshot> future = query.get();
            QuerySnapshot querySnapshot = future.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            if (documents.isEmpty()) {
                System.out.println("No user found with email: " + email);
                return null;
            }
            DocumentSnapshot document = documents.get(0);
            // Create and return the UserDetails object
            return User.withUsername(document.getString("email"))
                    .password(document.getString("hash"))  // Ensure 'hash' exists in the document
                    .roles("USER")
                    .build();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new UsernameNotFoundException("Error fetching user data", e);
        }
    }

    public UserDetails readUserById(String userId) throws Exception {
        try {
            System.out.println("Looking for user with ID: " + userId);

            // Retrieve the document reference for the given user ID
            DocumentReference docRef = firestore.collection("users").document(userId);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            // Check if the document exists
            if (!document.exists()) {
                System.out.println("No user found with ID: " + userId);
                return null;
            }

            // Create and return the UserDetails object
            return User.withUsername(document.getString("email"))
                    .password(document.getString("hash"))  // Ensure 'hash' exists in the document
                    .roles("USER")
                    .build();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new UsernameNotFoundException("Error fetching user data", e);
        }
    }

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
}
