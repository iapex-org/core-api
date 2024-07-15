package com.iapex.institution.DTO;

import java.util.List;


public class PatientDTO {

    private Long idPatient;
    private String hairColor;
    private String skinColor;
    private String eyeColor;
    private String sex;
    private double height;
    private double weight;
    private String birthDate;
    private int age;
    private String hairType;
    private String traits;
    private String build;
    private String posture;
    private String physicalConditions;
    private String name;
    private String fathername;
    private String mothername;
    private String bloodType;
    private String nationality;
    private String insuranceNumber;
	private String institutionName;
	private String nameRegister;

    private boolean status;

    private List<ImageDTO> images;

    // Constructor
    public PatientDTO() {}
    
    public PatientDTO(Long idPatient, String hairColor, String skinColor, String eyeColor, String sex, double height,
                      double weight, String birthDate, int age, String hairType, String traits, String build,
                      String posture, String physicalConditions, String name, String fathername, String mothername,
                      String bloodType, String nationality, String insuranceNumber, String institutionName, String nameRegister,
                      boolean status, List<ImageDTO> images) {
        this.idPatient = idPatient;
        this.hairColor = hairColor;
        this.skinColor = skinColor;
        this.eyeColor = eyeColor;
        this.sex = sex;
        this.height = height;
        this.weight = weight;
        this.birthDate = birthDate;
        this.age = age;
        this.hairType = hairType;
        this.traits = traits;
        this.build = build;
        this.posture = posture;
        this.physicalConditions = physicalConditions;
        this.name = name;
        this.fathername = fathername;
        this.mothername = mothername;
        this.bloodType = bloodType;
        this.nationality = nationality;
        this.insuranceNumber = insuranceNumber;
        this.institutionName = institutionName;
        this.nameRegister = nameRegister;
        this.status = status;
        this.images = images;
    }


    
    public List<ImageDTO> getImages() { return images; }
    public void setImages(List<ImageDTO> images) {        this.images = images; }
    // Getters and Setters
    public Long getIdPatient() {return idPatient; }
	public void setIdPatient(Long idPatient) {	this.idPatient = idPatient; }

    public String getHairColor() { return hairColor; }
    public void setHairColor(String hairColor) { this.hairColor = hairColor; }

    public String getSkinColor() { return skinColor; }
    public void setSkinColor(String skinColor) { this.skinColor = skinColor; }

    public String getEyeColor() { return eyeColor; }
    public void setEyeColor(String eyeColor) { this.eyeColor = eyeColor; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

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
    
	public String getInstitutionName() {return institutionName; }
	public void setInstitutionName(String institutionName) {		this.institutionName = institutionName;}
	
    public boolean getStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
    
    public String getNameRegister() {		return nameRegister; }
	public void setNameRegister(String nameRegister) { this.nameRegister = nameRegister; }

}