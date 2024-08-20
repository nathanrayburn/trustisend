package dev.test.social_login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        return http
                .authorizeHttpRequests( auth -> {
                    auth.requestMatchers("/").permitAll();  // set root access to everyone
                    auth.anyRequest().authenticated();        // any other requests has to be authenticated
                })
                .oauth2Login(withDefaults())                  // set oauth 2 login if we don't want to use a form login
                .formLogin(withDefaults())
                .build();
    }
}
