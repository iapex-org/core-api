package com.iapex.model;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Role {
	    USER(List.of(new SimpleGrantedAuthority("USER"))),
	    ADMIN(List.of(new SimpleGrantedAuthority("ADMIN")));

	    private final List<GrantedAuthority> authorities;

	    Role(List<GrantedAuthority> authorities) {
	        this.authorities = authorities;
	    }

	    public List<GrantedAuthority> getAuthorities() {
	        return authorities;
	    }
	
	    public static Role fromString(String roleStr) {
	        switch (roleStr.toUpperCase()) {
	            case "USER":
	                return USER;
	            case "ADMIN":
	                return ADMIN;
	            default:
	                throw new IllegalArgumentException("Role not recognized: " + roleStr);
	        }
	    }
}
