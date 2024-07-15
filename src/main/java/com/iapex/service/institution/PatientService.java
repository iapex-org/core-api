package com.iapex.service.institution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iapex.institution.DTO.ImageDTO;
import com.iapex.institution.DTO.PatientDTO;
import com.iapex.model.Response;
import com.iapex.model.institution.Patient;
import com.iapex.model.institution.Image;
import com.iapex.model.institution.Institution;
import com.iapex.repository.PatientRepository;
import com.iapex.repository.InstitutionRepository;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Transactional
    public Response registerPatient(PatientDTO request) throws Exception {
        try {
            // BUSCAR LA INSTITUCIÓN POR NOMBRE
            Institution institution = institutionRepository.findByName(request.getInstitutionName())
                .orElseThrow(() -> new Exception("Institución no encontrada"));
            // CREAR NUEVO PACIENTE
            Patient patient = new Patient();
            patient.setHairColor(request.getHairColor());
            patient.setSkinColor(request.getSkinColor());
            patient.setEyeColor(request.getEyeColor());
            patient.setGender(request.getGender());
            patient.setHeight(request.getHeight());
            patient.setWeight(request.getWeight());
            patient.setBirthDate(request.getBirthDate());
            patient.setAge(request.getAge());
            patient.setHairType(request.getHairType());
            patient.setTraits(request.getTraits());
            patient.setBuild(request.getBuild());
            patient.setPosture(request.getPosture());
            patient.setPhysicalConditions(request.getPhysicalConditions());
            patient.setName(request.getName());
            patient.setFathername(request.getFathername());
            patient.setMothername(request.getMothername());
            patient.setBloodType(request.getBloodType());
            patient.setNationality(request.getNationality());
            patient.setInsuranceNumber(request.getInsuranceNumber());
            patient.setInstitution(institution);
            // ESTABLECER EL STATUS EN FALSE
            patient.setStatus(false);
            // ASOCIAR IMÁGENES AL PACIENTE
            List<ImageDTO> imageDTOs = request.getImages();
            List<Image> images = new ArrayList<>();
            if (imageDTOs != null) {
                images = imageDTOs.stream()
                        .map(imageDTO -> {
                            Image image = new Image();
                            image.setImage(imageDTO.getImage());
                            image.setImageUrl(imageDTO.getImageUrl());
                            image.setPatient(patient);
                            return image;
                        })
                        .collect(Collectors.toList());
            }
            // ASIGNAR LA LISTA DE IMÁGENES AL PACIENTE
            patient.setImages(images);
            // GUARDAR EL PACIENTE
            patientRepository.save(patient);
            return new Response("El registro del paciente fue exitoso.");
        } catch (Exception e) {
            throw new Exception("Error al registrar el paciente: " + e.getMessage());
        }
    }
    
    
    // OBTENER PACIENTE POR ID
    public PatientDTO getPatientById(Long id) throws Exception {
    	Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new Exception("Paciente no encontrada"));
        return convertToDTO(patient);
    }
    
    
    
    //OBTENER POR ID SOLO SI STATUS ES FALSE ES DECIR NO ENCONTRADO
    public PatientDTO getPatientByIdFalse(Long id) throws Exception {
        Patient patient = patientRepository.findByIdPatientAndStatusFalse(id)
            .orElseThrow(() -> new Exception("Paciente no encontrado"));
        
        return convertToDTO(patient);
    }
    
    
    // OBTENER TODOS LOS PACIENTES
    public List<PatientDTO> getAllPatients() {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    
    
    // OBTENER TODAS LOS PACIENTES CON STATUS FALSE ES DECIR NO ENCONTRADOS
    public List<PatientDTO> getAllPatientsFalse() {
        List<Patient> patients = patientRepository.findByStatusFalse();
        return patients.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    

    private PatientDTO convertToDTO(Patient patient) {
        List<ImageDTO> imageDTOs = patient.getImages().stream()
                .map(image -> new ImageDTO(image.getIdImage(), image.getImage(), image.getImageUrl()))
                .collect(Collectors.toList());

        return new PatientDTO(
                patient.getIdPatient(),
                patient.getHairColor(),
                patient.getSkinColor(),
                patient.getEyeColor(),
                patient.getGender(),
                patient.getHeight(),
                patient.getWeight(),
                patient.getBirthDate(),
                patient.getAge(),
                patient.getHairType(),
                patient.getTraits(),
                patient.getBuild(),
                patient.getPosture(),
                patient.getPhysicalConditions(),
                patient.getName(),
                patient.getFathername(),
                patient.getMothername(),
                patient.getBloodType(),
                patient.getNationality(),
                patient.getInsuranceNumber(),
                patient.getInstitution().getName(),
                patient.getStatus(),
                imageDTOs
        );
    }
}