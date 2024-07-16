package com.iapex.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "images")
public class Image {


	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idImage;
	
    @Column(length = 100)
    private String image;
    
    private String imageUrl;
    
	@ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    
	public Long getIdImage() {		return idImage; }
	public void setIdImage(Long idImage) { this.idImage = idImage;}
    
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public Patient getPatient() { return patient; }
	public void setPatient(Patient patient) { this.patient = patient; }
}

