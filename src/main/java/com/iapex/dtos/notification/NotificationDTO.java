package com.iapex.dtos.notification;

import java.time.LocalDateTime;

public class NotificationDTO {

    private Long id;
    private Long contactRequestId;
    private String subject;
    private String body;
    private LocalDateTime sendDate;
    private LocalDateTime attendDateTime;
    private String attendingUser;

    // Constructor
    // Constructor actualizado
    public NotificationDTO(Long id, Long contactRequestId, String subject, String body,
            LocalDateTime sendDate, LocalDateTime attendDateTime,
            String attendingUser) {
        this.id = id;
        this.contactRequestId = contactRequestId;
        this.subject = subject;
        this.body = body;
        this.sendDate = sendDate;
        this.attendDateTime = attendDateTime;
        this.attendingUser = attendingUser;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getContactRequestId() {
        return contactRequestId;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getSendDate() {
        return sendDate;
    }

    public LocalDateTime getAttendDateTime() {
        return attendDateTime;
    }

    public String getAttendingUser() {
        return attendingUser;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setContactRequestId(Long contactRequestId) {
        this.contactRequestId = contactRequestId;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setSendDate(LocalDateTime sendDate) {
        this.sendDate = sendDate;
    }

    public void setAttendDateTime(LocalDateTime attendDateTime) {
        this.attendDateTime = attendDateTime;
    }
    public void setAttendingUser(String attendingUser) {
        this.attendingUser = attendingUser;
    }
}