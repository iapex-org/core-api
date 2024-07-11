package com.iapex.model.institution;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "markers")
public class Marker{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMark;
    
    @Column(length = 50)
    private String coordinates;
    
    @Column(length = 50)
    private String nameLoc;
    
    // Constructores
    public Marker() {}

    // Getters and Setters
    public Long getIdMark() { return idMark; }
    public void setIdMark(Long idMark) { this.idMark = idMark; }

    public String getCoordinates() { return coordinates; }
    public void setCoordinates(String coordinates) { this.coordinates = coordinates; }

    public String getNameLoc() { return nameLoc; }
    public void setNameLoc(String nameLoc) { this.nameLoc = nameLoc; }

}

