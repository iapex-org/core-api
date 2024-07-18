package com.iapex.dtos.contactRequest;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public class ContactRequestDTO {

    private Long id;

    @NotBlank(message = "El nombre del interesado es obligatorio")
    @Size(max = 100, message = "El nombre del interesado no puede tener más de 100 caracteres")
    private String interestedPersonName;

    @NotBlank(message = "El nombre de la persona que buscas es obligatorio")
    @Size(max = 100, message = "El nombre de la persona que buscas no puede tener más de 100 caracteres")
    private String missingPersonName;

    private Long patient;

    @NotBlank(message = "La relación con el paciente es obligatoria")
    @Size(max = 25, message = "La relación con el paciente no puede tener más de 25 caracteres")
    private String relationship;

    @Size(max = 10, min = 10, message = "El número de teléfono debe tener 10 caracteres")
    private String phoneNumber;

    @Size(max = 100, message = "El correo electrónico no puede tener más de 100 caracteres")
    private String email;

    private String message;

    private LocalDateTime requestDateTime;

    private String status;

    private String attendingUser;

    // Default constructor
    public ContactRequestDTO() {
    }

    // Constructor with all fields
    public ContactRequestDTO(Long Id, String interestedPersonName, String attendingUser,
            String missingPersonName, Long patient, String phoneNumber, String email, String relationship,
            LocalDateTime requestDateTime, String message, String status) {
        this.id = Id;
        this.interestedPersonName = interestedPersonName;
        this.attendingUser = attendingUser;
        this.missingPersonName = missingPersonName;
        this.patient = patient;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.relationship = relationship;
        this.requestDateTime = requestDateTime;
        this.message = message;
        this.status = status;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInterestedPersonName() {
        return interestedPersonName;
    }

    public void setInterestedPersonName(String interestedPersonName) {
        this.interestedPersonName = interestedPersonName;
    }

    public String getMissingPersonName() {
        return missingPersonName;
    }

    public void setMissingPersonName(String missingPersonName) {
        this.missingPersonName = missingPersonName;
    }

    public Long getPatient() {
        return patient;
    }

    public void setPatient(Long patient) {
        this.patient = patient;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getRequestDateTime() {
        return requestDateTime;
    }

    public void setRequestDateTime(LocalDateTime requestDateTime) {
        this.requestDateTime = requestDateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAttendingUser() {
        return attendingUser;
    }

    public void setAttendingUser(String attendingUser) {
        this.attendingUser = attendingUser;
    }    
}