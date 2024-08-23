package dev.test.social_login.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ui.Model;

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
}
