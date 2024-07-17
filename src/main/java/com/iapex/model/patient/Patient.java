package com.iapex.model.patient;

import java.time.LocalDateTime;
import java.util.List;
import com.iapex.model.user.UserWeb;
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
	private Long idPatient;

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

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_user_institution", referencedColumnName = "id_user_institution", nullable = false)
    private UserWeb registeringUser;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(length = 50, nullable = false)
    private String skinColor;

    @Column(length = 100, nullable = false)
    private String hair;

    @Column(length = 50, nullable = false)
    private String eyeColor;

    @Column(nullable = false)
    private Integer approximateHeight;

    @Column(length = 255)
    private String medicalConditions;

    @Column(length = 255)
    private String distinctiveFeatures;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;
	
	@Column(length = 255)
    private String additionalNotes;
	
	
	
	
	// Getters and Setters

	public Long getIdPatient() {
		return idPatient;
	}

	public void setIdPatient(Long idPatient) {
		this.idPatient = idPatient;
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

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
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
	
}