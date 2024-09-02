package com.example.demo.controller;

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

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @GetMapping("/secured")
    public String secured(){
        return "Hello, Secured!";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }
    @GetMapping("/home")
    public String home() {
        return "home";
    }
    @GetMapping("/upload")
    public String upload() {
        return "upload";
    }

    @GetMapping("/myfiles")
    public String myfiles() {
        return "myfiles";
    }

    @GetMapping("/download")
    public String download() {
        return "download";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }


}
