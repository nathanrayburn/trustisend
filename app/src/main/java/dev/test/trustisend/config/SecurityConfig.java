package dev.test.trustisend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import dev.test.trustisend.service.FirestoreUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {
        http.csrf()
            .disable()
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login", "/login.html", "/perform_login", "/css/**", "/js/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/perform_login")
                .defaultSuccessUrl("/home.html", true)
                .failureUrl("/login.html?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/perform_logout")
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler(logoutSuccessHandler())
            );
        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http, FirestoreUserDetailsService firestoreUserDetailsService, PasswordEncoder passwordEncoder) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(firestoreUserDetailsService)
                .passwordEncoder(passwordEncoder)
                .and()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new SimpleUrlLogoutSuccessHandler();
    }
}

