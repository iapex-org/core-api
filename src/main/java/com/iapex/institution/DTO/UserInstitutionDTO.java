package com.iapex.institution.DTO;

import com.iapex.model.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserInstitutionDTO {
	
	private Long idUserInstitution;

	@Size(max = 100, message = "El correo electrónico no puede tener más de 100 caracteres")
	@NotBlank(message = "El correo es obligatorio")	
	private String email;

	@Size(min = 8, max = 24, message = "La contraseña debe tener entre 8 y 24 caracteres")
	private String password;

	@Size(min = 4, max = 24, message = "El nombre debe tener entre 4 y 24 caracteres")
	private String name;

	@Size(min = 5, max = 50, message = "El apellido paterno debe tener entre 5 y 50 caracteres")
	private String fathername;

	@Size(min = 5, max = 50, message = "El apellido materno debe tener entre 5 y 50 caracteres")
	private String mothername;

	@NotBlank(message = "El nombre de la institución es obligatorio")	
	private String institutionName;

	@NotBlank(message = "El cargo es obligatorio")	
	private String charge;

	private Role role;
	
    private boolean status;  

    
    // Getters y setters existentes...
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
	
	public Long getIdUserInstitution() { return idUserInstitution; }
	public void setIdUserInstitution(Long idUserInstitution) {this.idUserInstitution = idUserInstitution; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFathername() { return fathername; }
    public void setFathername(String fathername) { this.fathername = fathername; }

    public String getMothername() { return mothername; }
    public void setMothername(String mothername) { this.mothername = mothername; }

    public String getCharge() { return charge; }
    public void setCharge(String charge) { this.charge = charge; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public String getInstitutionName() { return institutionName;	}
	public void setInstitutionName(String institutionName) {this.institutionName = institutionName; }
}


