package com.iapex.model.userMovil;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.iapex.config.AppUserDetails;
import com.iapex.model.role.Role;
import com.iapex.model.tokenMovil.Token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserMovil implements AppUserDetails  {
   
	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long idUser;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    private String password;
    
    @Column(length = 10)
    private String phone;

    @Column(name = "status", nullable = false)
    private boolean status;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private Role role;

    @JsonManagedReference
    @OneToMany(mappedBy = "userMovil")
    private List<Token> tokens;
    
    public Long getIdUser() { return idUser; }
    public void setIdUser(Long idUser) { this.idUser = idUser; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public boolean isConfirmed() { return status; }

    public boolean isAccountNonExpired() { return true; }

    public boolean isAccountNonLocked() { return true; }

    public boolean isCredentialsNonExpired() { return true; }

    public boolean isEnabled() { return true; }
    
    public Collection<? extends GrantedAuthority> getAuthorities() { return role.getAuthorities(); }

    public Role getRole() { return role; }

    public void setRole(Role role) { this.role = role; }

    public List<Token> getTokens() { return tokens; }

    public void setTokens(List<Token> tokens) { this.tokens = tokens; }

    @Override
    public String getUsername() { return this.email; }
    
    @Override
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}