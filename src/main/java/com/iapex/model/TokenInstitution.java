package com.iapex.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "token_institution")
public class TokenInstitution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "token")
    private String token;

    @Column(name = "is_logged_out")
    private boolean loggedOut;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "user_institution_id")
    private UserInstitution userInstitution; 

    @Column(name = "expiration_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isLoggedOut() { return loggedOut; }
    public void setLoggedOut(boolean loggedOut) { this.loggedOut = loggedOut; }

    public UserInstitution getUserInstitution() { return userInstitution; }
    public void setUserInstitution(UserInstitution userInstitution) { this.userInstitution = userInstitution; }

    public Date getExpirationDate() { return expirationDate; }
    public void setExpirationDate(Date expirationDate) { this.expirationDate = expirationDate; }

}



