package com.iapex.dtos.contactRequest;

public class ContactRequestCount {

    private String status;
    
    private Long count;
    
    public ContactRequestCount( String status, Long count) {
        this.status = status;
        this.count = count;
    }

    // Getters y Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
    
}

