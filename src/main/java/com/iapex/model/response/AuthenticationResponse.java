package com.iapex.model.response;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;


public class AuthenticationResponse {

	private String token;
    private String message;
    private Collection<? extends GrantedAuthority> authorities;

    public AuthenticationResponse(String token, String message, Collection<? extends GrantedAuthority> authorities) {
        this.token = token;
        this.message = message;
        this.authorities = authorities;
    }
    
    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) { this.authorities = authorities; }

}

