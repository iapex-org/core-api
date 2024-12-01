package com.iapex.dtos.notification;

import java.time.LocalDateTime;

public class NotificationDTO {

    // ID de la notificación
    private Long id; 
    // ID de la solicitud de contacto
    private Long contactRequestId;
    // Nombre de la persona desaparecida
    private String missingPersonName;
    // Nombre de la persona interesada
    private String interestedPersonName;
    // Fecha y hora de la solicitud
    private LocalDateTime requestDateTime;
    // Región de la solicitud
    private String region;
    // Estado de la notificación
    private boolean attended;

    // Constructor
    public NotificationDTO(Long id, Long contactRequestId, String missingPersonName, String interestedPersonName,
            LocalDateTime requestDateTime, String region, boolean attended) {
        this.id = id;
        this.contactRequestId = contactRequestId;
        this.missingPersonName = missingPersonName;
        this.interestedPersonName = interestedPersonName;
        this.requestDateTime = requestDateTime;
        this.region = region;
        this.attended = attended;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getContactRequestId() {
        return contactRequestId;
    }

    public void setContactRequestId(Long contactRequestId) {
        this.contactRequestId = contactRequestId;
    }

    public String getMissingPersonName() {
        return missingPersonName;
    }

    public void setMissingPersonName(String missingPersonName) {
        this.missingPersonName = missingPersonName;
    }

    public String getInterestedPersonName() {
        return interestedPersonName;
    }

    public void setInterestedPersonName(String interestedPersonName) {
        this.interestedPersonName = interestedPersonName;
    }

    public LocalDateTime getRequestDateTime() {
        return requestDateTime;
    }

    public void setRequestDateTime(LocalDateTime requestDateTime) {
        this.requestDateTime = requestDateTime;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isAttended() {
        return attended;
    }

    public void setAttended(boolean attended) {
        this.attended = attended;
    }
}
