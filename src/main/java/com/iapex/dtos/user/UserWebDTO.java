package com.iapex.dtos.user;

import com.iapex.models.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserWebDTO {

	private Long id;

	@Size(max = 50, message = "El nombre no puede tener más de 50 caracteres")
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @Size(max = 50, message = "El apellido paterno no puede tener más de 50 caracteres")
    @NotBlank(message = "El apellido paterno es obligatorio")
    private String lastName;

    @Size(max = 50, message = "El apellido materno no puede tener más de 50 caracteres")
    @NotBlank(message = "El apellido materno es obligatorio")
    private String secondLastName;

    @Size(max = 100, message = "El correo electrónico no puede tener más de 100 caracteres")
    @NotBlank(message = "El correo electrónico es obligatorio")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

	@Size(max = 100, message = "El nombre de la institución no puede tener más de 100 caracteres")
    @NotBlank(message = "El nombre de la institución es obligatorio")
    private String institution;

	@Size(max = 50, message = "El cargo no puede tener más de 50 caracteres")
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
