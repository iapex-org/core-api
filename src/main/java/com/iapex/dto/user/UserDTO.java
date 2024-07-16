package com.iapex.dto.user;

import com.iapex.model.user.Role;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserDTO {
	   
    @Size(max = 100, message = "El correo electrónico no puede tener más de 100 caracteres")
    @NotNull(message = "El correo es obligatorio")
    private String email;
    
    @NotNull(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 24, message = "La contraseña debe tener maximo 24 caracteres y minimo 8")
    private String password;
    
    @NotNull(message = "El telefono es obligatorio")
    @Size(min = 10, max = 10, message = "El número de teléfono debe tener maximo 10 caracteres")
    private String phone;
    
    private boolean status;
    
    private Role role;

    public UserDTO() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
    
    public Role getRole() { return role; }
   
    public void setRole(Role role) { this.role = role; }
}
