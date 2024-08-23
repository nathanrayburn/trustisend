package com.app.controller;

import com.google.cloud.spring.secretmanager.SecretManagerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecretController {

    @Autowired
    private SecretManagerTemplate secretManagerTemplate;

    @Autowired
    public SecretController(SecretManagerTemplate secretManagerTemplate) {
        this.secretManagerTemplate = secretManagerTemplate;
    }
    @GetMapping("/secret")
    public String getSecret() {
        // Retrieve the latest version of the secret
        String secretValue = secretManagerTemplate.getSecretString("projects/579596661856/secrets/gc-storage-key/versions/1");
        System.out.println(secretValue);
        return "secret";
    }
}
