package com.iapex.model.institution;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.iapex.config.AppUserDetails;
import com.iapex.model.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users_institution")
public class UserInstitution implements AppUserDetails   {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user_institution")
    private Long idUserInstitution;

    @Column(length = 50)
	private String name;
    @Column(length = 50)
    private String fathername;
    @Column(length = 50)
    private String mothername;
    @Column(length = 100)
    private String email;
    private String password;
    @Column(length = 50)
    private String charge;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_institution", referencedColumnName = "idInstitution")
    private Institution institution;

    @Column(name = "status", nullable = false)
    private boolean status;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private Role role;

    @JsonManagedReference
    @OneToMany(mappedBy = "userInstitution", cascade = CascadeType.ALL)
    private List<TokenInstitution> tokens;

    // CONSTRUCTOR VACIO
    public UserInstitution() {
    }
    
    //METODO PARA LA IMPLEMENTACION DE USERDETAILS
    @Override
    public String getUsername() { return this.getEmail(); }

    // Getters and Setters
    
    public Long getIdUserInstitution() {return idUserInstitution;}
	public void setIdUserInstitution(Long idUserInstitution) {this.idUserInstitution = idUserInstitution;	}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFathername() { return fathername; }
    public void setFathername(String fathername) { this.fathername = fathername; }

    public String getMothername() { return mothername; }
    public void setMothername(String mothername) { this.mothername = mothername; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCharge() { return charge; }
    public void setCharge(String charge) { this.charge = charge; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public List<TokenInstitution> getTokens() { return tokens; }

    public void setTokens(List<TokenInstitution> tokens) { this.tokens = tokens; }

    public Collection<? extends GrantedAuthority> getAuthorities() { return role.getAuthorities(); }

    public boolean isAccountNonExpired() { return true; }
    
    public boolean isConfirmed() { return status; }

    public boolean isAccountNonLocked() { return true; }

    public boolean isCredentialsNonExpired() { return true; }

    public boolean isEnabled() { return true; }
}


