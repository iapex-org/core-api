package com.iapex.dto.contactRequest;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Size;

public class ContactRequestDTO {

    private Long idContactRequest;

    @Size(max = 50, message = "El nombre del interesado no puede tener más de 50 caracteres")
    private String interestedName;

    private String attendedBy;

    @Size(max = 50, message = "El nombre de la persona que buscas no puede tener más de 50 caracteres")
    private String missingPersonName;

    private Long idPatient;

    private String patientName;

    @Size(max = 20, message = "El número de teléfono no puede tener más de 20 caracteres")
    private String phoneNumber;

    @Size(max = 100, message = "El correo electrónico no puede tener más de 100 caracteres")
    private String email;

    @Size(max = 25, message = "La relación con el paciente no puede tener más de 25 caracteres")
    private String relationship;

    private LocalDateTime requestDate;

    private String message;

    private String status;

    // Default constructor
    public ContactRequestDTO() {}

    // Constructor with all fields
    public ContactRequestDTO(Long idContactRequest, String interestedName, String attendedBy, String missingPersonName,
                             Long idPatient, String patientName, String phoneNumber, String email, String relationship,
                             LocalDateTime requestDate, String message, String status) {
        this.idContactRequest = idContactRequest;
        this.interestedName = interestedName;
        this.attendedBy = attendedBy;
        this.missingPersonName = missingPersonName;
        this.idPatient = idPatient;
        this.patientName = patientName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.relationship = relationship;
        this.requestDate = requestDate;
        this.message = message;
        this.status = status;
    }

    // Getters and setters
    public Long getIdContactRequest() { return idContactRequest; }
    public void setIdContactRequest(Long idContactRequest) { this.idContactRequest = idContactRequest; }

    public String getInterestedName() { return interestedName; }
    public void setInterestedName(String interestedName) { this.interestedName = interestedName; }

    public String getAttendedBy() { return attendedBy; }
    public void setAttendedBy(String attendedBy) { this.attendedBy = attendedBy; }

    public String getmissingPersonName() { return missingPersonName; }
    public void setMissingPersonName(String missingPersonName) { this.missingPersonName = missingPersonName; }

    public Long getIdPatient() { return idPatient; }
    public void setIdPatient(Long idPatient) { this.idPatient = idPatient; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}