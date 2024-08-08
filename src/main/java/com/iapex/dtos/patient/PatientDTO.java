package com.iapex.dtos.patient;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PatientDTO {

    private Long id;

    @Size(max = 50, message = "El nombre debe tener máximo 50 caracteres")
    private String name;

    @Size(max = 50, message = "El apellido paterno debe tener máximo 50 caracteres")
    private String lastName;

    @Size(max = 50, message = "El apellido materno debe tener máximo 50 caracteres")
    private String secondLastName;

    @Size(max = 10, message = "El sexo debe tener máximo 10 caracteres")
    private String gender;

    @NotNull(message = "La edad aproximada es obligatoria")
    private Integer approximateAge;

    private LocalDateTime registrationDateTime;

    private String registeringUser;

    private boolean active = true;

    @Size(max = 50, message = "El color de piel debe tener máximo 50 caracteres")
    private String skinColor;

    @Size(max = 100, message = "El cabello debe tener máximo 100 caracteres")
    private String hair;

    @Size(max = 50, message = "El tipo de complexión debe tener máximo 50 caracteres")
    private String complexion;

    @Size(max = 50, message = "El color de ojos debe tener máximo 50 caracteres")
    private String eyeColor;

    @NotNull(message = "La altura aproximada es obligatoria")
    private Integer approximateHeight;

    @Size(max = 255, message = "Las condiciones médicas deben tener máximo 255 caracteres")
    private String medicalConditions;

    @Size(max = 255, message = "Las características distintivas deben tener máximo 255 caracteres")
    private String distinctiveFeatures;

    private String institution;

    private List<ImageDTO> images;

    @Size(max = 255, message = "Las notas adicionales deben tener máximo 255 caracteres")
    private String additionalNotes;

    // Constructor
    public PatientDTO() {
    }

    // Constructor con todos los atributos
    public PatientDTO(Long id, String name, String lastName, String secondLastName, String gender,
            Integer approximateAge,
            LocalDateTime registrationDateTime, String registeringUser, boolean active, String skinColor,
            String hair, String complexion, String eyeColor, Integer approximateHeight, String medicalConditions,
            String distinctiveFeatures, String institution, List<ImageDTO> images, String additionalNotes) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.secondLastName = secondLastName;
        this.gender = gender;
        this.approximateAge = approximateAge;
        this.registrationDateTime = registrationDateTime;
        this.registeringUser = registeringUser;
        this.active = active;
        this.skinColor = skinColor;
        this.hair = hair;
        this.complexion = complexion;
        this.eyeColor = eyeColor;
        this.approximateHeight = approximateHeight;
        this.medicalConditions = medicalConditions;
        this.distinctiveFeatures = distinctiveFeatures;
        this.institution = institution;
        this.images = images;
        this.additionalNotes = additionalNotes;
    }

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

    public String getRegisteringUser() {
        return registeringUser;
    }

    public void setRegisteringUser(String registeringUser) {
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

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public List<ImageDTO> getImages() {
        return images;
    }

    public void setImages(List<ImageDTO> images) {
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

}