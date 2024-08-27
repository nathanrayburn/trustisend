package dev.test.social_login.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class FirestoreUserDetailsService implements UserDetailsService {

    private final Firestore firestore;

    @Autowired
    public FirestoreUserDetailsService(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            DocumentSnapshot document = firestore.collection("users").document(email).get().get();
            if (!document.exists()) {
                throw new UsernameNotFoundException("User not found");
            }

            String hash = document.getString("hash");
            return User.withUsername(email)
                    .password(hash)
                    .roles("USER")  // Assign roles as needed
                    .build();

        } catch (InterruptedException | ExecutionException e) {
            throw new UsernameNotFoundException("Error fetching user", e);
        }
    }
}
