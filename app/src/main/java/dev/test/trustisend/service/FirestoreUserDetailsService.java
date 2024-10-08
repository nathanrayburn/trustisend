package dev.test.trustisend.service;

import dev.test.trustisend.entity.ActiveFile;
import dev.test.trustisend.entity.Group;
import org.springframework.beans.factory.annotation.Autowired;
import dev.test.trustisend.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import dev.test.trustisend.util.FirestoreUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.LinkedList;

@Service
public class FirestoreUserDetailsService implements UserDetailsService {

    @Autowired
    private FirestoreUtil firestoreUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void registerNewUser(String email, String password) throws Exception {
        // Check if the user already exists
        User existingUser = firestoreUtil.readUserByEmail(email);
        if (existingUser != null) {
            throw new Exception("User already exists");
        }

        // Encrypt the password
        String encryptedPassword = passwordEncoder.encode(password);

        // Create a new user object
        User newUser = new User(email, encryptedPassword);

        // Use FirestoreUtil to create the user in Firestore
        firestoreUtil.createUser(newUser);
    }
    public void incrementDownloadCount(String groupUUID) throws Exception {
        try {
            firestoreUtil.incrementDownloadCount(groupUUID);
        } catch (Exception e) {
            throw new Exception("Error fetching user data", e);
        }
    }
    public LinkedList<Group> getGroups(String email) throws  Exception{
        try {
            User user = firestoreUtil.readUserByEmail(email);
            if (user == null) {
                throw new Exception("User not found with email: " + email);
            }
            return firestoreUtil.readGroupsByEmail(email);
        } catch (Exception e) {
            throw new Exception("Error fetching user data", e);
        }
    }

    public LinkedList<ActiveFile> getFiles(String groupUUID ) throws Exception {
        try {
            return firestoreUtil.readActiveFilesByGroupUUID(groupUUID);
        } catch (Exception e) {
            throw new Exception("Error fetching user data", e);
        }
    }

    @Override
    public User loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            User user = firestoreUtil.readUserByEmail(email);
            if (user == null) {
                throw new UsernameNotFoundException("User not found with email: " + email);
            }
            return user;
        } catch (Exception e) {
            throw new UsernameNotFoundException("Error fetching user data", e);
        }
    }
}
