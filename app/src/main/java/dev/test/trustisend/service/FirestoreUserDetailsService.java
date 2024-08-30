package dev.test.trustisend.service;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import dev.test.trustisend.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import dev.test.trustisend.util.FirestoreUtil;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreUserDetailsService implements UserDetailsService {

    private final Firestore firestore;
    @Autowired
    private FirestoreUtil firestoreUtil;

    @Autowired
    public FirestoreUserDetailsService(Firestore firestore) {
        this.firestore = firestore;
    }

    public User createNewUser(String email, String hash){
        dev.test.trustisend.entity.User user =  new dev.test.trustisend.entity.User(email, hash);
        try {
        return firestoreUtil.createUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            // Debug log to check if the method is called
            System.out.println("Looking for user with email: " + email);

            // Perform the query
            Query query = firestore.collection("users").whereEqualTo("email", email);
            ApiFuture<QuerySnapshot> future = query.get();
            QuerySnapshot querySnapshot = future.get();

            // Check if we received any documents
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            if (documents.isEmpty()) {
                System.out.println("No user found with email: " + email);
                throw new UsernameNotFoundException("User not found");
            }

            // Extract user data
            DocumentSnapshot document = documents.get(0);
            // System.out.println("User data found: " + document.getData()); // it prints the password hash

            // Create and return the UserDetails object
            return org.springframework.security.core.userdetails.User.withUsername(document.getString("email"))
                    .password(document.getString("hash"))  // Ensure 'hash' exists in the document
                    .roles("USER")
                    .build();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new UsernameNotFoundException("Error fetching user data", e);
        }
    }
}