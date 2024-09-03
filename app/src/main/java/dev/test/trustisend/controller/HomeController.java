package dev.test.trustisend.controller;

import dev.test.trustisend.entity.InputFile;
import dev.test.trustisend.service.FirestoreUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import dev.test.trustisend.entity.User;

import org.springframework.ui.Model;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Controller
public class  HomeController {

    @Autowired
    private FirestoreUserDetailsService userDetailsService;

    @GetMapping("/")
    public String home(Model model){
        model.addAttribute("test","Test value message");
        return "index";
    }
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

    @GetMapping("/secured")
    public String secured(){
        return "Hello, Secured!";
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
    public String upload(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // Vérifiez si l'utilisateur est connecté
        if (userDetails != null) {
            String email = userDetails.getUsername(); // Récupère l'email ou username de l'utilisateur
            model.addAttribute("email", email);
        }
        return "upload";
    }

    @GetMapping("/myfiles")
    public String myfiles(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // Vérifiez si l'utilisateur est connecté
        if (userDetails != null) {
            String email = userDetails.getUsername(); // Récupère l'email ou username de l'utilisateur
            model.addAttribute("email", email);
        }
        return "myfiles";
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

    @PostMapping("/upload")
    public String createLink(@RequestParam("files") MultipartFile[] files,
                             @AuthenticationPrincipal User user,
                             Model model) {
       /* if (user != null) {
            // 1. Create a group in Firestore for the user's files
            String groupId = firestoreService.createGroup(user);

            // 2. Upload the files and associate them with the group
            List<InputFile> uploadedFiles = fileService.uploadFiles(files, groupId);

            // 3. Save file metadata to Firestore under the created group
            firestoreService.addFilesToGroup(groupId, uploadedFiles);

            // Redirect or update the model as needed
            return "home"; // Adjust redirect as needed
        }*/
        return "redirect:/login"; // If user is not authenticated

    }

}
