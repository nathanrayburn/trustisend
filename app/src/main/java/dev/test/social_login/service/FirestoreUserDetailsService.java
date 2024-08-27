package dev.test.social_login.service;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
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
            Query query = firestore.collection("users").whereEqualTo("email", email);
            DocumentSnapshot document = query.get().get().getDocuments().get(0);
            if (document.exists()) {
                System.out.println(document.getData());
                return User.withUsername(document.getString("email"))
                        .password(document.getString("hash"))
                        .roles("USER")
                        .build();

            } else {
                throw new UsernameNotFoundException("User not found");
            }

        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
        
    }
}