package com.iapex.model.institution;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "contact")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idContact;
    
    @Column(length = 10)
    private String phone;
    
    @Column(length = 100)
    private String adress;
    
    @Column(length = 100)
    private String website;
    
    // Constructores
    public Contact() {
    }

    // Getters and Setters
    public Long getIdContact() { return idContact; }
    public void setIdContact(Long idContact) { this.idContact = idContact; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAdress() { return adress; }
    public void setAdress(String adress) { this.adress = adress; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

}


