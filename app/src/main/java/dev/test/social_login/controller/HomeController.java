package dev.test.social_login.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ui.Model;

import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class  HomeController {

    @Value("${secret.filepath}")
    private String secretFilePath = "";
    @GetMapping("/")
    public String home(Model model){
        model.addAttribute("test","Test value message");

        try{
            System.out.println(getSecret());

        }catch (Exception e){
            System.out.println(e);
        }


        return "index";
    }
    @GetMapping("/secured")
    public String secured(){
        return "Hello, Secured!";
    }

    private String getSecret() throws Exception{
        String secret = new String(Files.readAllBytes(Paths.get(secretFilePath)));

        return secret;
    }
}
