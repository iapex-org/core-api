package com.iapex.dtos.contactRequest;

public class UpdateContactRequestDTO {

    private String status;

    private String attendingUser;

    // Getters and setters
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
