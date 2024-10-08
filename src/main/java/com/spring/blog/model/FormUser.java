package com.spring.blog.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@Data
@Builder
public class FormUser implements ProviderUser {

    private String id;
    private String username;
    private String password;
    private String email;
    private String provider;
    private String profileImageUrl;
    private List<? extends GrantedAuthority> authorities;

    @Override
    public String getId() {
        return "[Form]" + id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    @Override
    public List<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
