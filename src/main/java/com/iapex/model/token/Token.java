package com.iapex.model.token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.iapex.model.userMovil.UserMovil;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "token")
    private String token;

    @Column(name = "is_logged_out")
    private boolean loggedOut;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "user_id")
    private UserMovil userMovil;

    
    @Column(name = "expiration_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;

    public Date getExpirationDate() { return expirationDate; }
    public void setExpirationDate(Date expirationDate) { this.expirationDate = expirationDate; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isLoggedOut() { return loggedOut; }
    public void setLoggedOut(boolean loggedOut) { this.loggedOut = loggedOut; }

    public UserMovil getUserMovil() { return userMovil; }
    public void setUserMovil(UserMovil userMovil) { this.userMovil = userMovil; }

}

