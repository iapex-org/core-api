package com.iapex.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iapex.models.institution.Institution;
import com.iapex.models.user.UserWeb;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    // Identificador único de la notificación
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Asunto de la notificación
    @Column(length = 100, nullable = false)
    private String subject;

    // Cuerpo o mensaje de la notificación
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    // Fecha y hora de envío de la notificación
    @Column(nullable = false)
    private LocalDateTime sendDate;

    // Fecha y hora en que se atendió la notificación
    private LocalDateTime attendDateTime;

    // Usuario que atendió la notificación
    @ManyToOne
    @JoinColumn(name = "attending_user_id")
    private UserWeb attendingUser;

    // Relación con una solicitud de contacto
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_request_id", nullable = false)
    private ContactRequest contactRequest;

    // Institución relacionada con la notificación
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    // Constructor vacío
    public Notification() {
    }

    // Constructor con argumentos
    public Notification(ContactRequest contactRequest, String subject, String body, LocalDateTime sendDate,
                        Institution institution, UserWeb attendingUser, LocalDateTime attendDateTime) {
        this.contactRequest = contactRequest;
        this.subject = subject;
        this.body = body;
        this.sendDate = sendDate;
        this.institution = institution;
        this.attendingUser = attendingUser;
        this.attendDateTime = attendDateTime;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDateTime getSendDate() {
        return sendDate;
    }

    public void setSendDate(LocalDateTime sendDate) {
        this.sendDate = sendDate;
    }

    public LocalDateTime getAttendDateTime() {
        return attendDateTime;
    }

    public void setAttendDateTime(LocalDateTime attendDateTime) {
        this.attendDateTime = attendDateTime;
    }

    public UserWeb getAttendingUser() {
        return attendingUser;
    }

    public void setAttendingUser(UserWeb attendingUser) {
        this.attendingUser = attendingUser;
    }

    public ContactRequest getContactRequest() {
        return contactRequest;
    }

    public void setContactRequest(ContactRequest contactRequest) {
        this.contactRequest = contactRequest;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }
}
