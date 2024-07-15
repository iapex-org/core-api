package com.iapex.institution.DTO;

import java.time.LocalDateTime;

public class ConversationDTO {

    private Long idConversation;
    private String interestedName;
    private int patient;
    private String phoneNumber;
    private String email;
    private String patientRelationship;
    private LocalDateTime requestDate;
    private String message;
    private String status;

    // Default constructor
    public ConversationDTO() {}

    // Constructor with all fields
    public ConversationDTO(Long idConversation, String interestedName, int patient, String phoneNumber, String email,
                           String patientRelationship, LocalDateTime requestDate, String message, String status) {
        this.idConversation = idConversation;
        this.interestedName = interestedName;
        this.patient = patient;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.patientRelationship = patientRelationship;
        this.requestDate = requestDate;
        this.message = message;
        this.status = status;
    }

    // Getters and setters
    public Long getIdConversation() { return idConversation; }
    public void setIdConversation(Long idConversation) { this.idConversation = idConversation; }

    public String getInterestedName() { return interestedName; }
    public void setInterestedName(String interestedName) { this.interestedName = interestedName; }

    public int getPatient() { return patient; }
    public void setPatient(int patient) { this.patient = patient; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPatientRelationship() { return patientRelationship; }
    public void setPatientRelationship(String patientRelationship) { this.patientRelationship = patientRelationship; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}