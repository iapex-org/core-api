package com.iapex.DTO;

import jakarta.validation.constraints.NotNull;

public class UserAuthenticationDTO {
	
	private String username;
	
    @NotNull(message = "El email es obligatorio para enviar un mensaje")
	private String email;
    
    @NotNull(message = "La contraseña es obligatoria")
    private String password;
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
}
