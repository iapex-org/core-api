package com.iapex.service.patient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iapex.dto.patient.ImageDTO;
import com.iapex.dto.patient.PatientDTO;
import com.iapex.model.institution.Institution;
import com.iapex.model.patient.Image;
import com.iapex.model.patient.Patient;
import com.iapex.model.response.Response;
import com.iapex.model.user.UserWeb;
import com.iapex.repository.institution.InstitutionRepository;
import com.iapex.repository.patient.PatientRepository;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Transactional
    public Response registerPatient(PatientDTO request, String name, String fatherName, String motherName) throws Exception {
        try {
            // BUSCAR LA INSTITUCIÓN POR NOMBRE
            Institution institution = institutionRepository.findByName(request.getInstitutionName())
                .orElseThrow(() -> new Exception("Institución no encontrada"));
            // CREAR NUEVO PACIENTE
            Patient patient = new Patient();
            patient.setHairColor(request.getHairColor());
            patient.setSkinColor(request.getSkinColor());
            patient.setEyeColor(request.getEyeColor());
            patient.setSex(request.getSex());
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
            patient.setAdditionalNotes(request.getAdditionalNotes());
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
            
            // CREAR EL NOMBRE COMPLETO DE LA PERSONA QUE REGISTRA
            String fullName = buildFullName(name, fatherName, motherName);
            patient.setNameRegister(fullName);

            // GUARDAR EL PACIENTE
            patientRepository.save(patient);
            return new Response("El registro del paciente fue exitoso.");
        } catch (Exception e) {
            throw new Exception("Error al registrar el paciente: " + e.getMessage());
        }
    }

    private String buildFullName(String name, String fatherName, String motherName) {
        return String.format("%s %s %s", 
                name != null ? name : "",
                fatherName != null ? fatherName : "",
                motherName != null ? motherName : "")
                .trim().replaceAll("\\s+", " ");
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
    
    @Transactional
    public Response updateById(Long id, PatientDTO request) {
        try {
            // BUSCAR EL PACIENTE POR SU ID O LANZAR UNA EXCEPCIÓN SI NO SE ENCUENTRA
            Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new Exception("Paciente no encontrado"));
            
            // ACTUALIZAR EL ESTADO DEL PACIENTE SI ES DIFERENTE
            if (patient.getStatus() != request.getStatus()) {
                patient.setStatus(request.getStatus());
                
                // GUARDAR LOS CAMBIOS
                patientRepository.save(patient);
                return new Response("Estado del paciente actualizado exitosamente");
            } else {
                return new Response("El estado proporcionado es igual al estado actual. No se realizaron cambios.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("Error al actualizar el estado del paciente: " + e.getMessage());
        }
    }
    
    
    // OBTIENE LAS pacientes DE LA MISMA INSTITUCIÓN QUE EL USUARIO AUTENTICADO
 // OBTENER PACIENTES DE LA MISMA INSTITUCIÓN QUE EL USUARIO AUTENTICADO
    public List<PatientDTO> getPatientsByAuthenticatedUser() {
        try {
            // OBTENER LA INFORMACIÓN DEL USUARIO AUTENTICADO
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserWeb userInstitution = (UserWeb) authentication.getPrincipal();
            // OBTENER LA INSTITUCIÓN DEL USUARIO AUTENTICADO
            Institution institution = userInstitution.getInstitution();

            // OBTENER LOS PACIENTES DE LA MISMA INSTITUCIÓN
            List<Patient> patients = patientRepository.findByInstitution(institution);

            // CONVERTIR LOS PACIENTES A DTOs
            return patients.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            // MANEJAR LA EXCEPCIÓN ADECUADAMENTE SEGÚN TUS REQUERIMIENTOS
            return Collections.emptyList(); // O PODRÍAS RETORNAR UN MENSAJE DE ERROR
        }
    }

    
    

    private PatientDTO convertToDTO(Patient patient) {
        List<ImageDTO> imageDTOs = patient.getImages().stream()
                .map(image -> new ImageDTO(image.getIdImage(), image.getImage(), image.getImageUrl()))
                .collect(Collectors.toList());

        return new PatientDTO(
                patient.getHairColor(),
                patient.getSkinColor(),
                patient.getEyeColor(),
                patient.getSex(),
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
                patient.getNameRegister(),
                patient.getAdditionalNotes(),
                patient.getStatus(),
                imageDTOs
        );
    }
}

