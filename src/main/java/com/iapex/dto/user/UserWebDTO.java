package com.iapex.dto.user;

import com.iapex.model.role.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserWebDTO {

	private Long id;

	@Size(min = 4, max = 24, message = "El nombre debe tener entre 4 y 24 caracteres")
	private String name;
	
	@Size(min = 5, max = 50, message = "El apellido paterno debe tener entre 5 y 50 caracteres")
	private String lastName;
	
	@Size(min = 5, max = 50, message = "El apellido materno debe tener entre 5 y 50 caracteres")
	private String secondLastName;
	
	@Size(max = 100, message = "El correo electrónico no puede tener más de 100 caracteres")
	@NotBlank(message = "El correo es obligatorio")
	private String email;

	@Size(min = 8, max = 24, message = "La contraseña debe tener entre 8 y 24 caracteres")
	private String password;

	@NotBlank(message = "El nombre de la institución es obligatorio")
	private String institution;
	
	@NotBlank(message = "El cargo es obligatorio")
	private String position;

	private boolean accountVerified;

	private Role role;

	// Getters y setters
	public boolean isAccountVerified() {
		return accountVerified;
	}

	public void setAccountVerified(boolean accountVerified) {
		this.accountVerified = accountVerified;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

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

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getSecondLastName() {
		return secondLastName;
	}

	public void setSecondLastName(String secondLastName) {
		this.secondLastName = secondLastName;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}
}
