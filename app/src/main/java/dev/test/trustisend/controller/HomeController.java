package dev.test.trustisend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import dev.test.trustisend.entity.User;

import org.springframework.ui.Model;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;


@Controller
public class  HomeController {
    @GetMapping("/")
    public String home(Model model){
        model.addAttribute("test","Test value message");
        return "index";
    }
    @GetMapping("/secured")
    public String secured(){
        return "Hello, Secured!";
    }
    @GetMapping("/login")
    public String login(Model model){
        return "login";
    }
    @GetMapping("/home")
    public String home(@AuthenticationPrincipal User user, Model model) {
        if (user != null) {
            String email = user.getEmail();

            System.out.println("User email: " + email);

            model.addAttribute("email", email);
        } else {
            model.addAttribute("email", "Guest");
        }
        return "home";
    }
}
