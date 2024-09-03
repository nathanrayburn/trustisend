package dev.test.trustisend.controller;


import dev.test.trustisend.entity.*;
import dev.test.trustisend.service.FileService;
import dev.test.trustisend.service.FirestoreUserDetailsService;
import dev.test.trustisend.util.FirestoreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@Controller
public class  HomeController {

    @Autowired
    private FileService fileService;

    @Autowired
    private FirestoreUserDetailsService userDetailsService;

    @Autowired
    private FirestoreUtil firestoreUtil;

    @PostMapping("/perform_signup")
    public String performSignup(@RequestParam("email") String email,
                                @RequestParam("password") String password,
                                @RequestParam("re-password") String rePassword,
                                Model model) {
        // Check if passwords match
        if (!password.equals(rePassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "signup";
        }


        if (!isPasswordStrong(password)) {
            model.addAttribute("error", "Password does not meet the strength requirements.");
            return "signup";
        }

        try {
            userDetailsService.registerNewUser(email, password);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "signup";
        }
    }

    private boolean isPasswordStrong(String password) {
        return password.length() >= 8 &&
                password.matches(".*[a-z].*") &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[0-9].*") &&
                password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
    }
    
    @GetMapping("/login")
    public String login(Model model){
        return "login";
    }
     @GetMapping("/signup")
    public String signup(Model model){
        return "signup";
    }

    @GetMapping("/upload")
    public String upload(@AuthenticationPrincipal User user, Model model){

        if (user != null) {
            String email = user.getEmail();

            model.addAttribute("email", email);
            return "upload";
        }
        return "login";
    }

    @GetMapping("/myfiles")
    public String myfiles(Model model){
        return "myfiles";
    }

    @GetMapping("/")
    public String index(@AuthenticationPrincipal User user,Model model){
        if(user != null){
            String email = user.getEmail();
            model.addAttribute("email",email);
            return "home";
        }
        return "index";
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal User user, Model model) {

        if (user != null) {
            String email = user.getEmail();

            model.addAttribute("email", email);
            return "home";
        }

        return "redirect:/login";


    }

    @PostMapping("/download")
    ResponseEntity<Resource> downloadFolder(@RequestParam("uid") String uID) throws IOException {

        try{

            File folder = fileService.downloadFolder(uID);

            Path path = Paths.get(folder.getAbsolutePath());
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

            userDetailsService.incrementDownloadCount(uID);

            return ResponseEntity.ok()
                    .contentLength(folder.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }


    }

    @PostMapping("/link")
    public String folderContent(@RequestParam("uid") String uID, @AuthenticationPrincipal User user ,Model model){
        // get files of the group in firestore

        // pass the files to the model
        // return the files to the view
        return "home";
    }

    /**
     * Get all the links of the user
     * @param user
     * @param model
     * @return
     */
    @GetMapping("/myLinks")
    public String myLinks(@AuthenticationPrincipal User user, Model model) {
        if (user != null) {
            String email = user.getEmail();

            try{
                // Get groups of the user
                LinkedList<Group> groups = userDetailsService.getGroups(email);

                // Map to store group UUIDs and their corresponding files
                Map<String, LinkedList<ActiveFile>> groupFilesMap = new HashMap<>();

                // Get files for each group and put them in the map
                for (Group group : groups) {
                    LinkedList<ActiveFile> files = userDetailsService.getFiles(group.getGroupUUID());
                    groupFilesMap.put(group.getGroupUUID(), files);
                }

                // Add the map to the model
                model.addAttribute("groupFilesMap", groupFilesMap);

                return "myLinks";
            }catch (Exception e){
                model.addAttribute("error", "Error fetching user data: " + e.getMessage());
                return "myLinks";
            }

        }
        return "redirect:/login";
    }

    @PostMapping("/upload")
    public String createLink(@RequestParam("files") MultipartFile[] files,
                             @AuthenticationPrincipal User user,
                             Model model) throws Exception {
        if (user != null) {
            // 1. Create a group in Firestore for the user's files
            Group group = firestoreUtil.createGroup(new Group(user.getEmail(), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), 0));
            String uID = group.getGroupUUID();
            // 2. Upload the files and associate them with the group
            List<InputFile> uploadedFiles = fileService.uploadFiles(files, uID);

            // 3. Save file metadata to Firestore under the created group
            for(InputFile file : uploadedFiles) {

                ActiveFile f = new ActiveFile(group, file.getFileName(), FileScanStatus.PENDING);
                f = firestoreUtil.createActiveFile(f);
            }

            // Redirect or update the model as needed
            return "home"; // Adjust redirect as needed
        }
        return "redirect:/login"; // If user is not authenticated

    }

}
