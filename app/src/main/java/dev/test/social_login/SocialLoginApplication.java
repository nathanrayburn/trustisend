package dev.test.social_login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
public class SocialLoginApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialLoginApplication.class, args);
	}

}

@Component
class SecretReader implements CommandLineRunner {

	@Value("${secret.filepath}")
	private String secretFilePath;

	@Override
	public void run(String... args) throws Exception {
		String secret = new String(Files.readAllBytes(Paths.get(secretFilePath)));
		System.out.println("Secret: " + secretFilePath);
	}
}