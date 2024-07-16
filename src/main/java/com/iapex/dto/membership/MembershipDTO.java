package com.iapex.dto.membership;

import java.time.LocalDateTime;

public class MembershipDTO {
    private Long idMembership;
    private LocalDateTime startDate;
    private LocalDateTime  endDate;
    private boolean status;
    private String institutionName;
    
    // Constructor
    public MembershipDTO() {
    }
    
    // Constructor
    public MembershipDTO(Long idMembership, LocalDateTime startDate, LocalDateTime endDate, boolean status, String institutionName) {
        this.idMembership = idMembership;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.institutionName = institutionName;
    }


    public Long getIdMembership() { return idMembership; }
    public void setIdMembership(Long idMembership) { this.idMembership = idMembership; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
    
    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }
}
