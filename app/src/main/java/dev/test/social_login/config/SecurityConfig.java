package dev.test.social_login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder; // Add this import statement
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.*;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user1 = User.withUsername("user1")
            .password(passwordEncoder().encode("user1Pass"))
            .roles("USER")
            .build();
        UserDetails user2 = User.withUsername("user2")
            .password(passwordEncoder().encode("user2Pass"))
            .roles("USER")
            .build();
        UserDetails admin = User.withUsername("admin")
            .password(passwordEncoder().encode("adminPass"))
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(user1, user2, admin);
    }

    @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf()
        .disable()
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/anonymous*").anonymous()
            .requestMatchers("/login*").permitAll()
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login.html")
            .loginProcessingUrl("/perform_login")
            .defaultSuccessUrl("/homepage.html", true)
            .failureUrl("/login.html?error=true")
            .failureHandler(authenticationFailureHandler())
        )
        .logout(logout -> logout
            .logoutUrl("/perform_logout")
            .deleteCookies("JSESSIONID")
            .logoutSuccessHandler(logoutSuccessHandler())
        );

    return http.build();
}

 // Custom AuthenticationFailureHandler bean
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        // SimpleUrlAuthenticationFailureHandler redirects to a URL on authentication failure
        return new SimpleUrlAuthenticationFailureHandler("/login.html?error=true");
    }

    // Custom LogoutSuccessHandler bean
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        // SimpleUrlLogoutSuccessHandler redirects to a URL on logout success
        return new SimpleUrlLogoutSuccessHandler();
    }


    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
