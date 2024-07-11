package com.iapex.config;

import org.springframework.security.core.userdetails.UserDetails;

public interface AppUserDetails extends UserDetails {
	//CAMPOS NESESARIOS PARA LA IMPLEMENTACION DE LOGIN 
    String getEmail();
    String getPassword();
}
