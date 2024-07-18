package com.iapex.models.institution;

import java.util.Date;
import java.util.List;

import org.checkerframework.common.aliasing.qual.Unique;

import com.iapex.models.Membership;
import com.iapex.models.patient.Patient;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "institutions")
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String openingHours;
    
    @Column(columnDefinition = "TEXT")
    private String mapUrl;

    @Column(columnDefinition = "TEXT")
    private String emails;

    @Column(length = 100)
    private String image;

    private String imageUrl;

    @Column(length = 50)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String phoneNumbers;

    @Column(columnDefinition = "TEXT")
    private String websites;
    
    @Unique
    @Column(length = 50, nullable = false)
    private String verificationKey;

    @Temporal(TemporalType.DATE)
    private Date registrationDateTime;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "direction_id", referencedColumnName = "id")
    private Direction direction;

    @OneToOne(mappedBy = "institution", cascade = CascadeType.ALL)
    private Membership membership;

    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL)
    private List<Patient> patients;

    @Column(nullable = false)
    private boolean active;

    // Constructor vacío
    public Institution() {
    }

    // Getters y Setters
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

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public String getMapUrl() {
        return mapUrl;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
    }

    public String getEmails() {
        return emails;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(String phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public String getWebsites() {
        return websites;
    }

    public void setWebsites(String websites) {
        this.websites = websites;
    }

    public String getVerificationKey() {
        return verificationKey;
    }

    public void setVerificationKey(String verificationKey) {
        this.verificationKey = verificationKey;
    }

    public Date getRegistrationDateTime() {
        return registrationDateTime;
    }

    public void setRegistrationDateTime(Date registrationDateTime) {
        this.registrationDateTime = registrationDateTime;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Membership getMembership() {
        return membership;
    }

    public void setMembership(Membership membership) {
        this.membership = membership;
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}