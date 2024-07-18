package com.iapex.models.user;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.iapex.config.AppUserDetails;
import com.iapex.enums.RoleEnum;
import com.iapex.models.ContactRequest;
import com.iapex.models.institution.Institution;
import com.iapex.models.token.TokenWeb;
import jakarta.persistence.FetchType;
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
@Table(name = "users_web")
public class UserWeb implements AppUserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50, nullable = false)
    private String lastName;

    @Column(length = 50)
    private String secondLastName;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "institution_id", referencedColumnName = "id", nullable = false)
    private Institution institution;

    @Column(length = 50, nullable = false)
    private String position;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean accountVerified;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    @JsonManagedReference
    @OneToMany(mappedBy = "userWeb", cascade = CascadeType.ALL)
    private List<TokenWeb> tokens;

    @OneToMany(mappedBy = "attendingUser", fetch = FetchType.LAZY)
    private List<ContactRequest> contactRequests;

    // Constructor
    public UserWeb() {
    }

    // Metodo para la implementacion de la interfaz UserDetails
    @Override
    public String getUsername() {
        return this.getEmail();
    }

    // Getters and Setters
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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public boolean isAccountVerified() {
        return accountVerified;
    }

    public void setAccountVerified(boolean accountVerified) {
        this.accountVerified = accountVerified;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }

    public List<TokenWeb> getTokens() {
        return tokens;
    }

    public void setTokens(List<TokenWeb> tokens) {
        this.tokens = tokens;
    }

    public List<ContactRequest> getContactRequests() {
        return contactRequests;
    }

    public void setContactRequests(List<ContactRequest> contactRequests) {
        this.contactRequests = contactRequests;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isConfirmed() {
        return accountVerified;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }
}