package com.iapex.model.patient;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "conversations")
public class Conversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conversation")
    private Long idConversation;
    
    @Column(length = 50)
    private String interestedName;
    
    private int patient;
    
    @Column(length = 10)
    private String phoneNumber;
    
    @Column(length = 100)
    private String email;
    
    @Column(length = 15)
    private String patientRelationship;
    
    private LocalDateTime requestDate;
    
    @Column(columnDefinition="TEXT")
    private String message;
    
    @Column(length = 15)
    private String status;

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
