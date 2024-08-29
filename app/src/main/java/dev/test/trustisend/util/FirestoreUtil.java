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

    public String createUser(Map<String, Object> userData) throws Exception {
        CollectionReference users = firestore.collection("users");
        ApiFuture<DocumentReference> result = users.add(userData);
        String userId = result.get().getId();
        System.out.println("User created with ID: " + userId);
        return userId;
    }

    public UserDetails readUser(String email) throws Exception{
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

}
