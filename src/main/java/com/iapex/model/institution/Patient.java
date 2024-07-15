package com.iapex.model.institution;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "patients")
public class Patient {


	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPatient;

    @Column(length = 20)
    private String hairColor;
    
    @Column(length = 15)
    private String skinColor;
    
    @Column(length = 15)
    private String eyeColor;
    
    @Column(length = 15)
    
    private String gender;
    
    private double height;
    
    private double weight;
    
    @Column(length = 25)
    private String birthDate;
    
    private int age;
    
    @Column(length = 20)
    private String hairType;
    
    private String traits;
    
    @Column(length = 20)
    private String build;
    
    @Column(length = 20)
    private String posture;
    
    private String physicalConditions;

    @Column(length = 50)
    private String name;

    @Column(length = 50)
    private String fathername;

    @Column(length = 50)
    private String mothername;

    @Column(length = 10)
    private String bloodType;
    
    @Column(length = 20)
    private String nationality;
    
    @Column(length = 11)
    private String insuranceNumber;
    
    private boolean status;
    
    @ManyToOne
    @JoinColumn(name = "id_institution", referencedColumnName = "idInstitution")
    private Institution institution; 
    
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<Image> images;

    public List<Image> getImages() { return images; }
    public void setImages(List<Image> images) { this.images = images; }

    // Getters and Setters
    public Long getIdPatient() {return idPatient; }
	public void setIdPatient(Long idPatient) {	this.idPatient = idPatient; }

    public String getHairColor() { return hairColor; }
    public void setHairColor(String hairColor) { this.hairColor = hairColor; }

    public String getSkinColor() { return skinColor; }
    public void setSkinColor(String skinColor) { this.skinColor = skinColor; }

    public String getEyeColor() { return eyeColor; }
    public void setEyeColor(String eyeColor) { this.eyeColor = eyeColor; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getHairType() { return hairType; }
    public void setHairType(String hairType) { this.hairType = hairType; }

    public String getTraits() { return traits; }
    public void setTraits(String traits) { this.traits = traits; }

    public String getBuild() { return build; }
    public void setBuild(String build) { this.build = build; }

    public String getPosture() { return posture; }
    public void setPosture(String posture) { this.posture = posture; }

    public String getPhysicalConditions() { return physicalConditions; }
    public void setPhysicalConditions(String physicalConditions) { this.physicalConditions = physicalConditions; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFathername() { return fathername; }
    public void setFathername(String fathername) { this.fathername = fathername; }

    public String getMothername() { return mothername; }
    public void setMothername(String mothername) { this.mothername = mothername; }

    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    
    public String getInsuranceNumber() { return insuranceNumber; }
    public void setInsuranceNumber(String insuranceNumber) { this.insuranceNumber = insuranceNumber; }
    
    public boolean getStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
    
    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }
}