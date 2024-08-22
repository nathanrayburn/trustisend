package dev.test.social_login.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class  HomeController {
    @GetMapping("/")
    public String home(){
        return "This will be the landing page.";
    }
    @GetMapping("/secured")
    public String secured(){
        return "Hello, Secured!";
    }
}
