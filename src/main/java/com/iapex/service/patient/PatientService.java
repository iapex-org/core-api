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
import com.iapex.repository.user.UserWebRepository;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private UserWebRepository userWebRepository;

    @Transactional
    public Response registerPatient(PatientDTO request)
            throws Exception {
        try {
            // Buscar institución por nombre
            Institution institution = institutionRepository.findByName(request.getInstitution())
                    .orElseThrow(() -> new Exception("Institución no encontrada"));
            // Buscar registering user por nombre completo
            UserWeb registeringUser = userWebRepository.findByNameAndLastNameAndSecondLastName(
                    request.getName(), request.getLastName(), request.getSecondLastName())
                    .orElseThrow(() -> new Exception("Usuario no encontrado"));

            // Crear un nuevo paciente
            Patient patient = new Patient();
            patient.setName(request.getName());
            patient.setLastName(request.getLastName());
            patient.setSecondLastName(request.getSecondLastName());
            patient.setGender(request.getGender());
            patient.setApproximateAge(request.getApproximateAge());
            patient.setRegistrationDateTime(request.getRegistrationDateTime());
            patient.setRegisteringUser(registeringUser);
            patient.setSkinColor(request.getSkinColor());
            patient.setHair(request.getHair());
            patient.setComplexion(request.getComplexion());
            patient.setEyeColor(request.getEyeColor());
            patient.setApproximateHeight(request.getApproximateHeight());
            patient.setMedicalConditions(request.getMedicalConditions());
            patient.setDistinctiveFeatures(request.getDistinctiveFeatures());
            patient.setInstitution(institution);
            patient.setAdditionalNotes(request.getAdditionalNotes());
            // Establecer el estado del paciente en false (no encontrado)
            patient.setActive(false);
            // Asociar las imágenes al paciente
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
            // Asignar la lista de imágenes al paciente
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

    // OBTENER POR ID SOLO SI STATUS ES FALSE ES DECIR NO ENCONTRADO
    public PatientDTO getPatientByIdFalse(Long id) throws Exception {
        Patient patient = patientRepository.findByIdAndActiveFalse(id)
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
        List<Patient> patients = patientRepository.findByActiveFalse();
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
            if (patient.getActive() != request.isActive()) {
                patient.setActive(request.isActive());

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
            UserWeb userWeb = (UserWeb) authentication.getPrincipal();
            // OBTENER LA INSTITUCIÓN DEL USUARIO AUTENTICADO
            Institution institution = userWeb.getInstitution();

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
                .map(image -> new ImageDTO(image.getId(), image.getImage(), image.getImageUrl()))
                .collect(Collectors.toList());

        return new PatientDTO(
                patient.getName(),
                patient.getLastName(),
                patient.getSecondLastName(),
                patient.getGender(),
                patient.getApproximateAge(),
                patient.getRegistrationDateTime(),
                patient.getRegisteringUser().getName(),
                patient.getActive(),
                patient.getSkinColor(),
                patient.getHair(),
                patient.getComplexion(),
                patient.getEyeColor(),
                patient.getApproximateHeight(),
                patient.getMedicalConditions(),
                patient.getDistinctiveFeatures(),
                patient.getInstitution().getName(),
                imageDTOs,
                patient.getAdditionalNotes());
    }
}