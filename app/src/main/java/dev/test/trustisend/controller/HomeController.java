package dev.test.trustisend.controller;

import dev.test.trustisend.entity.ActiveFile;
import dev.test.trustisend.entity.Group;
import dev.test.trustisend.entity.InputFile;
import dev.test.trustisend.service.FileService;
import dev.test.trustisend.service.FirestoreUserDetailsService;
import dev.test.trustisend.util.Zipper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import dev.test.trustisend.entity.User;
import dev.test.trustisend.service.FirestoreUserDetailsService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
public class  HomeController {

    @Autowired
    private FileService fileService;

    @Autowired
    private FirestoreUserDetailsService userDetailsService;


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

        File folder = fileService.downloadFolder(uID);

        Path path = Paths.get(folder.getAbsolutePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

        return ResponseEntity.ok()
                .contentLength(folder.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
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
     * @return Map<GroupUUID, LinkedList<ActiveFile>>
     */
    @GetMapping("/myLinks")
    public String myLinks(@AuthenticationPrincipal User user, Model model) {
        if (user != null) {
            String email = user.getEmail();

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
        }
        return "redirect:/login";
    }

    @PostMapping("/upload")
    public String createLink(@RequestParam("files") MultipartFile[] files,
                             @AuthenticationPrincipal User user,
                             Model model) {
        // fileService.uploadFiles(files);
        if(user != null){

            // create group in firestore

            // retrieve group id
            // pass the group unique id into the uploadFiles method
            List<InputFile> inputFiles = fileService.uploadFiles(files);

            // add files to group in firestore

            model.addAttribute("inputFiles",inputFiles);

            return "home";
        }

        return "redirect:/login";
    }

}
