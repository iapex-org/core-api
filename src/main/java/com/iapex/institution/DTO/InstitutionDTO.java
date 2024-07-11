package com.iapex.institution.DTO;

public class InstitutionDTO {

    private Long id;
    private String name;
    private String email;
	private String typeInstitution;
    private String openingHours;
    private String history;
    private String image;
    private String imageUrl;
    private boolean status;
	private String contactPhone;
    private String contactAddress;
    private String contactWebsite;
    private String markerCoordinates;
    private String markerNameLoc;
    
    // Constructor vacío
    public InstitutionDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getContactAddress() { return contactAddress; }
    public void setContactAddress(String contactAddress) { this.contactAddress = contactAddress; }

    public String getContactWebsite() { return contactWebsite; }
    public void setContactWebsite(String contactWebsite) { this.contactWebsite = contactWebsite; }

    public String getMarkerCoordinates() { return markerCoordinates; }
    public void setMarkerCoordinates(String markerCoordinates) { this.markerCoordinates = markerCoordinates; }

    public String getMarkerNameLoc() { return markerNameLoc; }
    public void setMarkerNameLoc(String markerNameLoc) { this.markerNameLoc = markerNameLoc; }
    
    public String getTypeInstitution() { return typeInstitution;}
	public void setTypeInstitution(String typeInstitution) { this.typeInstitution = typeInstitution; }

}
