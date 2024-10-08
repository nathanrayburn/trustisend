package dev.test.trustisend.entity;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// User class to represent a user in the application
public class User implements UserDetails {

    @Id
    private String id;
    private String email;
    private String hash;

    public User() {
    }

    public User(String email, String hash) {
        this.email = email;
        this.hash = hash;
    }

    public User(String id, String email, String hash) {
        this.id = id;
        this.email = email;
        this.hash = hash;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public String getHash() {
        return hash;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return hash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    //following functions are inherited but not used
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}