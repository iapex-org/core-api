package com.iapex.models;

import java.time.LocalDateTime;

import com.iapex.models.patient.Patient;
import com.iapex.models.user.UserWeb;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "contact_requests")
public class ContactRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String interestedPersonName;

    @Column(length = 100, nullable = false)
    private String missingPersonName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(length = 25, nullable = false)
    private String relationship;

    @Column(length = 10)
    private String phoneNumber;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private LocalDateTime requestDateTime;

    @Column(length = 20, nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attending_user_id")
    private UserWeb attendingUser;

    // Getters y setters
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

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
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

    public UserWeb getAttendingUser() {
        return attendingUser;
    }

    public void setAttendingUser(UserWeb attendingUser) {
        this.attendingUser = attendingUser;
    }
}