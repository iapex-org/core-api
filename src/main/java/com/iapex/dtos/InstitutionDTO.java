package com.iapex.dtos;

import java.util.Date;
import jakarta.validation.constraints.Size;

public class InstitutionDTO {

    private Long id;

    @Size(max = 100, message = "El nombre de la institución no puede tener más de 100 caracteres")
    private String name;

    @Size(max = 50, message = "El tipo de institución no puede tener más de 50 caracteres")
    private String type;

    @Size(max = 50, message = "El estado no debe exceder los 50 caracteres")
    private String state;

    @Size(max = 50, message = "La ciudad no debe exceder los 50 caracteres")
    private String city;

    @Size(max = 25, message = "El código postal no debe exceder los 25 caracteres")
    private String postalCode;

    @Size(max = 50, message = "El vecindario no debe exceder los 50 caracteres")
    private String neighborhood;

    @Size(max = 50, message = "La calle no debe exceder los 50 caracteres")
    private String street;

    @Size(max = 25, message = "El número de dirección no debe exceder los 25 caracteres")
    private String number;

    @Size(max = 100, message = "El horario de apertura no puede tener más de 100 caracteres")
    private String openingHours;

    @Size(max = 255, message = "Los correos electrónicos no pueden tener más de 255 caracteres")
    private String emails;

    @Size(max = 255, message = "Los números de teléfono no pueden tener más de 255 caracteres")
    private String phoneNumbers;
    
    @Size(max = 255, message = "Las URLs de los sitios web no pueden tener más de 255 caracteres")
    private String websites;

    private Date registrationDateTime;

    private String image;

    private String imageUrl;

    private String mapUrl;

    @Size(max = 50, message = "La clave de verificación no puede tener más de 50 caracteres")
    private String verificationKey;

    private boolean active;

    // Constructor
    public InstitutionDTO() {
    }
    
    // Getters and setters
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

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public String getMapUrl() {
        return mapUrl;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
    }

    public String getEmails() {
        return emails;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(String phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public String getWebsites() {
        return websites;
    }

    public void setWebsites(String websites) {
        this.websites = websites;
    }

    public String getVerificationKey() {
        return verificationKey;
    }

    public void setVerificationKey(String verificationKey) {
        this.verificationKey = verificationKey;
    }

    public Date getRegistrationDateTime() {
        return registrationDateTime;
    }

    public void setRegistrationDateTime(Date registrationDateTime) {
        this.registrationDateTime = registrationDateTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getState() {
        return state;
    }

    public void setState(String directionState) {
        this.state = directionState;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String directionCity) {
        this.city = directionCity;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String directionPostalCode) {
        this.postalCode = directionPostalCode;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String directionNeighborhood) {
        this.neighborhood = directionNeighborhood;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String directionStreet) {
        this.street = directionStreet;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String directionNumber) {
        this.number = directionNumber;
    }
}