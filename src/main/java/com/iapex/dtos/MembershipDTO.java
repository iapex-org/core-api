package com.iapex.dtos;

import java.time.LocalDateTime;

public class MembershipDTO {

    private Long id;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private boolean status;
    
    private String institutionName;

    // Constructor
    public MembershipDTO() {
    }

    // Constructor
    public MembershipDTO(Long id, LocalDateTime startDate, LocalDateTime endDate, boolean status,
            String institutionName) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.institutionName = institutionName;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }
}
