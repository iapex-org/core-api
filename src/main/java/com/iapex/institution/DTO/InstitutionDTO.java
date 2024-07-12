package com.iapex.institution.DTO;

import jakarta.validation.constraints.Size;

public class InstitutionDTO {
    @Size(max = 50, message = "El nombre de la institución no puede tener más de 50 caracteres")
    private String name;

    @Size(max = 100, message = "El correo electrónico no puede tener más de 100 caracteres")
    private String email;

    @Size(max = 50, message = "El tipo de institución no puede tener más de 50 caracteres")
    private String typeInstitution;

    @Size(max = 100, message = "El horario de apertura no puede tener más de 100 caracteres")
    private String openingHours;

    private String history;

    @Size(max = 100, message = "La URL de la imagen no puede tener más de 100 caracteres")
    private String image;

    private String imageUrl;

    private boolean status;
    
    @Size(max = 100, message = "El numero maximo de caracteres para el campo telefono es de 100 caracteres")
    private String contactPhone;

    @Size(max = 100, message = "La URL del sitio web no puede tener más de 100 caracteres")
    private String contactWebsite;

    private String directionUrlMapsInstitution;

    @Size(max = 35, message = "El estado no debe exceder los 35 caracteres")
    private String directionState;

    @Size(max = 35, message = "El municipio no debe exceder los 35 caracteres")
    private String directionMunicipality;

    @Size(max = 35, message = "El código postal no debe exceder los 35 caracteres")
    private String directionPostalCode;

    @Size(max = 35, message = "La colonia no debe exceder los 35 caracteres")
    private String directionColony;

    @Size(max = 35, message = "La calle no debe exceder los 35 caracteres")
    private String directionStreet;

    @Size(max = 35, message = "El número de dirección no debe exceder los 35 caracteres")
    private String directionNumber;

    // Constructor vacío
    public InstitutionDTO() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }

    public String getHistory() { return history; }
    public void setHistory(String history) { this.history = history; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactWebsite() { return contactWebsite; }
    public void setContactWebsite(String contactWebsite) { this.contactWebsite = contactWebsite; }

    public String getTypeInstitution() { return typeInstitution; }
    public void setTypeInstitution(String typeInstitution) { this.typeInstitution = typeInstitution; }
    
    public String getDirectionUrlMapsInstitution() { return directionUrlMapsInstitution; }
    public void setDirectionUrlMapsInstitution(String directionUrlMapsInstitution) { this.directionUrlMapsInstitution = directionUrlMapsInstitution; }
    
    public String getDirectionState() { return directionState; }
    public void setDirectionState(String directionState) { this.directionState = directionState; }
    
    public String getDirectionMunicipality() { return directionMunicipality; }
    public void setDirectionMunicipality(String directionMunicipality) { this.directionMunicipality = directionMunicipality; }
    
    public String getDirectionPostalCode() { return directionPostalCode; }
    public void setDirectionPostalCode(String directionPostalCode) { this.directionPostalCode = directionPostalCode; }
    
    public String getDirectionColony() { return directionColony; }
    public void setDirectionColony(String directionColony) { this.directionColony = directionColony; }
    
    public String getDirectionStreet() { return directionStreet; }
    public void setDirectionStreet(String directionStreet) { this.directionStreet = directionStreet; }
    
    public String getDirectionNumber() { return directionNumber; }
    public void setDirectionNumber(String directionNumber) { this.directionNumber = directionNumber; }
}
