package com.iapex.dto.user;

import jakarta.validation.constraints.NotNull;

public class UserWebAuthenticationDTO {

    @NotNull(message = "El email es obligatorio")
    private String email;

    @NotNull(message = "La contraseña es obligatoria")
    private String password;

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
