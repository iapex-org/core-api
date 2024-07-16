package com.iapex.dto.patient;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Size;

public class ConversationDTO {

    private Long idConversation;
    
    @Size(max = 50, message = "El nombre del interesado no puede tener más de 50 caracteres")
    private String interestedName;
    
    private String attendeddBy;
    
    @Size(max = 50, message = "El nombre de la persona que buscas no puede tener más de 50 caracteres")
    private String searcherName;
    
    private Long idPatient;
    
    private String patientName;
    
    @Size(max = 10, message = "El número de teléfono no puede tener más de 10 caracteres")
    private String phoneNumber;
    
    @Size(max = 100, message = "El correo electrónico no puede tener más de 100 caracteres")
    private String email;
    
    @Size(max = 25, message = "La relación con el paciente no puede tener más de 25 caracteres")
    private String patientRelationship;
    
    private LocalDateTime requestDate;
    
    private String message;
    
    
    private String status;

    // Default constructor
    public ConversationDTO() {}

    // Constructor with all fields
    public ConversationDTO(Long idConversation, String interestedName, String attendeddBy, String searcherName,
    		Long idPatient, String patientName, String phoneNumber, String email, String patientRelationship,
                           LocalDateTime requestDate, String message, String status) {
        this.idConversation = idConversation;
        this.interestedName = interestedName;
        this.attendeddBy = attendeddBy;
        this.searcherName = searcherName;
        this.idPatient = idPatient;
        this.patientName = patientName;
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

    public String getAttendeddBy() { return attendeddBy; }
    public void setAttendeddBy(String attendeddBy) { this.attendeddBy = attendeddBy; }

    public String getSearcherName() { return searcherName; }
    public void setSearcherName(String searcherName) { this.searcherName = searcherName; }

    public Long getIdPatient() { return idPatient; }
    public void setIdPatient(Long idPatient) { this.idPatient = idPatient; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

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
