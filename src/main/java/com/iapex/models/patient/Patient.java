package com.iapex.models.patient;

import java.time.LocalDateTime;
import java.util.List;

import com.iapex.models.ContactRequest;
import com.iapex.models.institution.Institution;
import com.iapex.models.user.UserWeb;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "patients")
public class Patient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50)
    private String lastName;

    @Column(length = 50)
    private String secondLastName;

    @Column(length = 10, nullable = false)
    private String gender;

    @Column(nullable = false)
    private Integer approximateAge;

    @Column(nullable = false)
    private LocalDateTime registrationDateTime;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(length = 50, nullable = false)
    private String skinColor;

    @Column(length = 100, nullable = false)
    private String hair;

	@Column(length = 50, nullable = false)
	private String complexion;

    @Column(length = 50, nullable = false)
    private String eyeColor;

    @Column(nullable = false)
    private Integer approximateHeight;

    @Column(length = 255)
    private String medicalConditions;

    @Column(length = 255)
    private String distinctiveFeatures;

	@ManyToOne
	@JoinColumn(name = "registering_user_id", referencedColumnName = "id", nullable = true)
	private UserWeb registeringUser;

	@ManyToOne
	@JoinColumn(name = "institution_id", referencedColumnName = "id", nullable = false)
	private Institution institution;
	
	@OneToMany(mappedBy = "patient", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
	private List<ContactRequest> contactRequests;

	@OneToMany(mappedBy = "patient", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
	private List<Image> images;
	
	@Column(length = 255)
    private String additionalNotes;

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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getApproximateAge() {
        return approximateAge;
    }

    public void setApproximateAge(Integer approximateAge) {
        this.approximateAge = approximateAge;
    }

    public LocalDateTime getRegistrationDateTime() {
        return registrationDateTime;
    }

    public void setRegistrationDateTime(LocalDateTime registrationDateTime) {
        this.registrationDateTime = registrationDateTime;
    }

    public UserWeb getRegisteringUser() {
        return registeringUser;
    }

    public void setRegisteringUser(UserWeb registeringUser) {
        this.registeringUser = registeringUser;
    }
	
    public String getSkinColor() {
        return skinColor;
    }

    public void setSkinColor(String skinColor) {
        this.skinColor = skinColor;
    }

    public String getHair() {
        return hair;
    }

    public void setHair(String hair) {
        this.hair = hair;
    }

    public String getComplexion() {
        return complexion;
    }

    public void setComplexion(String complexion) {
        this.complexion = complexion;
    }

    public String getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(String eyeColor) {
        this.eyeColor = eyeColor;
    }

    public Integer getApproximateHeight() {
        return approximateHeight;
    }

    public void setApproximateHeight(Integer approximateHeight) {
        this.approximateHeight = approximateHeight;
    }

    public String getMedicalConditions() {
        return medicalConditions;
    }

    public void setMedicalConditions(String medicalConditions) {
        this.medicalConditions = medicalConditions;
    }

    public String getDistinctiveFeatures() {
        return distinctiveFeatures;
    }

    public void setDistinctiveFeatures(String distinctiveFeatures) {
        this.distinctiveFeatures = distinctiveFeatures;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    public List<ContactRequest> getContactRequests() {
        return contactRequests;
    }

    public void setContactRequests(List<ContactRequest> contactRequests) {
        this.contactRequests = contactRequests;
    }
}