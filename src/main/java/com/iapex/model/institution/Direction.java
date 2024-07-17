package com.iapex.model.institution;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "directions")
public class Direction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDirection;

    @Column(columnDefinition = "TEXT")
    private String urlMapsInstitution;

    @Column(length = 35)
    private String state;

    @Column(length = 35)
    private String municipality;

    @Column(length = 35)
    private String postalCode;

    @Column(length = 35)
    private String colony;

    @Column(length = 35)
    private String street;

    @Column(length = 35)
    private String number;

    // Constructor
    public Direction() {
    }

    // Getters and Setters
    public Long getIdDirection() {
        return idDirection;
    }

    public void setIdDirection(Long idDirection) {
        this.idDirection = idDirection;
    }

    public String getUrlMapsInstitution() {
        return urlMapsInstitution;
    }

    public void setUrlMapsInstitution(String urlMapsInstitution) {
        this.urlMapsInstitution = urlMapsInstitution;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getColony() {
        return colony;
    }

    public void setColony(String colony) {
        this.colony = colony;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}