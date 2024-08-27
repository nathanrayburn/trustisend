package dev.test.trustisend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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
}
