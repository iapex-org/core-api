package com.iapex.institution.DTO;

import jakarta.validation.constraints.NotNull;

public class UserInstitutionAuthenticationDTO {
		
    @NotNull(message = "El email es obligatorio")
	private String email;
    
    @NotNull(message = "La contraseña es obligatoria")
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
}
