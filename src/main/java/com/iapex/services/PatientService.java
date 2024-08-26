package com.iapex.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iapex.dtos.patient.ImageDTO;
import com.iapex.dtos.patient.PatientDTO;
import com.iapex.models.ContactRequest;
import com.iapex.models.institution.Institution;
import com.iapex.models.patient.Image;
import com.iapex.models.patient.Patient;
import com.iapex.models.response.Response;
import com.iapex.models.user.UserWeb;
import com.iapex.repositories.ContactRequestRepository;
import com.iapex.repositories.PatientRepository;
import com.iapex.repositories.institution.InstitutionRepository;
import com.iapex.repositories.user.UserWebRepository;
import com.iapex.services.files.StorageService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private InstitutionRepository institutionRepository;
    
    @Autowired
    private StorageService storageService;

    @Autowired
    private UserWebRepository userWebRepository;
    
    @Autowired
    private ContactRequestRepository contactRequestRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    @Transactional
    public Response registerPatient(PatientDTO request) throws Exception {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = ((UserDetails) authentication.getPrincipal()).getUsername();

            // Buscar el usuario autenticado por correo electrónico
            UserWeb authenticatedUser = userWebRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new Exception("Usuario no encontrado"));

            // Obtener la institución del usuario autenticado
            Institution institution = authenticatedUser.getInstitution();
            if (institution == null) {
                throw new Exception("El usuario autenticado no está asociado a ninguna institución");
            }

            // Crear un nuevo paciente
            Patient patient = new Patient();
            patient.setName(request.getName());
            patient.setLastName(request.getLastName());
            patient.setSecondLastName(request.getSecondLastName());
            patient.setGender(request.getGender());
            patient.setApproximateAge(request.getApproximateAge());
            patient.setRegistrationDateTime(LocalDateTime.now());
            patient.setRegisteringUser(authenticatedUser);
            patient.setSkinColor(request.getSkinColor());
            patient.setHair(request.getHair());
            patient.setComplexion(request.getComplexion());
            patient.setEyeColor(request.getEyeColor());
            patient.setApproximateHeight(request.getApproximateHeight());
            patient.setMedicalConditions(request.getMedicalConditions());
            patient.setDistinctiveFeatures(request.getDistinctiveFeatures());
            patient.setInstitution(institution);  // Asignar la institución del usuario autenticado
            patient.setAdditionalNotes(request.getAdditionalNotes());
            patient.setActive(true);

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
            patient.setImages(images);

            // Guardar el paciente
            patientRepository.save(patient);
            return new Response("El registro del paciente fue exitoso.");
        } catch (Exception e) {
            throw new Exception("Error al registrar el paciente: " + e.getMessage());
        }
    }
    @Transactional
    public Response updatePatient(Long id, PatientDTO request) throws Exception {
        try {
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> new Exception("Paciente no encontrado"));
    
            // Obtener el usuario autenticado actualmente
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
    
            // Buscar el usuario autenticado por correo electrónico
            UserWeb authenticatedUser = userWebRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new Exception("Usuario no encontrado"));
    
            // Actualizar el usuario registrante
            patient.setRegisteringUser(authenticatedUser);
    
            // Actualizar otros campos solo si han cambiado
            if (!Objects.equals(patient.getName(), request.getName())) patient.setName(request.getName());
            if (!Objects.equals(patient.getLastName(), request.getLastName())) patient.setLastName(request.getLastName());
            if (!Objects.equals(patient.getSecondLastName(), request.getSecondLastName())) patient.setSecondLastName(request.getSecondLastName());
            if (!Objects.equals(patient.getGender(), request.getGender())) patient.setGender(request.getGender());
            if (!Objects.equals(patient.getApproximateAge(), request.getApproximateAge())) patient.setApproximateAge(request.getApproximateAge());
            if (!Objects.equals(patient.getSkinColor(), request.getSkinColor())) patient.setSkinColor(request.getSkinColor());
            if (!Objects.equals(patient.getHair(), request.getHair())) patient.setHair(request.getHair());
            if (!Objects.equals(patient.getComplexion(), request.getComplexion())) patient.setComplexion(request.getComplexion());
            if (!Objects.equals(patient.getEyeColor(), request.getEyeColor())) patient.setEyeColor(request.getEyeColor());
            if (!Objects.equals(patient.getApproximateHeight(), request.getApproximateHeight())) patient.setApproximateHeight(request.getApproximateHeight());
            if (!Objects.equals(patient.getMedicalConditions(), request.getMedicalConditions())) patient.setMedicalConditions(request.getMedicalConditions());
            if (!Objects.equals(patient.getDistinctiveFeatures(), request.getDistinctiveFeatures())) patient.setDistinctiveFeatures(request.getDistinctiveFeatures());
            if (!Objects.equals(patient.getAdditionalNotes(), request.getAdditionalNotes())) patient.setAdditionalNotes(request.getAdditionalNotes());
            if (patient.isActive() != request.isActive()) patient.setActive(request.isActive());
    
            // Verificar si la lista de imágenes es nula o vacía
            if (request.getImages() != null && !request.getImages().isEmpty()) {
                // Validar la cantidad de imágenes
                if (request.getImages().size() < 8 || request.getImages().size() > 12) {
                    throw new Exception("Debe proporcionar entre 8 y 12 imágenes.");
                }
    
                // Crear un mapa de las imágenes existentes por su nombre de archivo
                Map<String, Image> existingImages = patient.getImages().stream()
                        .collect(Collectors.toMap(Image::getImage, image -> image));
    
                List<Image> updatedImages = new ArrayList<>();
                for (ImageDTO imageDTO : request.getImages()) {
                    Image existingImage = existingImages.get(imageDTO.getImage());
                    if (existingImage == null || !existingImage.getImageUrl().equals(imageDTO.getImageUrl())) {
                        // Si es una imagen nueva o ha cambiado, crear o actualizar la entidad Image
                        Image image = (existingImage != null) ? existingImage : new Image();
                        image.setImage(imageDTO.getImage());
                        image.setImageUrl(imageDTO.getImageUrl());
                        image.setPatient(patient);
                        updatedImages.add(image);
    
                        // Si la imagen existía pero ha cambiado, eliminar la versión anterior
                        if (existingImage != null) {
                            storageService.deleteFile(existingImage.getImage());
                        }
                    } else {
                        // Si la imagen es la misma, simplemente la añadimos a la lista actualizada
                        updatedImages.add(existingImage);
                    }
                    // Remover la imagen del mapa de existentes
                    existingImages.remove(imageDTO.getImage());
                }
    
                // Eliminar las imágenes que ya no se usan
                for (Image oldImage : existingImages.values()) {
                    storageService.deleteFile(oldImage.getImage());
                }
    
                // Actualizar la colección de imágenes del paciente
                patient.getImages().clear();
                patient.getImages().addAll(updatedImages);
            }
    
            patientRepository.save(patient);
            return new Response("El paciente ha sido actualizado exitosamente.");
        } catch (Exception e) {
            throw new Exception("Error al actualizar el paciente: " + e.getMessage());
        }
    }
    
    
    

   
    
    // OBTENER PACIENTE POR ID
    public PatientDTO getPatientById(Long id) throws Exception {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new Exception("Paciente no encontrada"));
        return convertToDTO(patient);
    }


    // OBTENER POR ID SOLO SI STATUS ES FALSE ES DECIR NO ENCONTRADO
    public PatientDTO getPatientByIdTrue(Long id) throws Exception {
        Patient patient = patientRepository.findByIdAndActiveTrue(id)
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


    // OBTENER TODAS LOS PACIENTES CON STATUS TRUE ES DECIR NO ENCONTRADOS
    public List<PatientDTO> getAllPatientsTrue() {
        List<Patient> patients = patientRepository.findByActiveTrueAndInstitution_ActiveTrue();
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
            if (patient.isActive() != request.isActive()) {
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
    
    //transactionTemplate para tener mas control sobre la transaccion
    @Transactional
    public Response deletePatient(Long id) {
        return transactionTemplate.execute(new TransactionCallback<Response>() {
            @Override
            public Response doInTransaction(TransactionStatus status) {
                try {
                    Patient patient = patientRepository.findById(id)
                        .orElseThrow(() -> new Exception("Paciente no encontrado con ID: " + id));

                    if (!patient.getId().equals(id)) {
                        throw new Exception("ID del paciente no coincide con el solicitado");
                    }
                    for (Image image : patient.getImages()) {
                        storageService.deleteFile(image.getImage());
                    }
                   List<ContactRequest> contactRequests = patient.getContactRequests();
                    contactRequestRepository.deleteAll(contactRequests);

                    patientRepository.deleteById(id);
                    return new Response("Paciente y todos sus datos asociados han sido eliminados exitosamente.");
                } catch (Exception e) {
                    status.setRollbackOnly();
                    return new Response("Error al eliminar el paciente: " + e.getMessage());
                }
            }
        });
    }


    private PatientDTO convertToDTO(Patient patient) {
        List<ImageDTO> imageDTOs = patient.getImages().stream()
                .map(image -> new ImageDTO(image.getId(), image.getImage(), image.getImageUrl()))
                .collect(Collectors.toList());

        // Crear el nombre completo del usuario registrante
        String registeringUserFullName = String.format("%s %s %s",
                patient.getRegisteringUser().getName(),
                patient.getRegisteringUser().getLastName(),
                patient.getRegisteringUser().getSecondLastName());

        return new PatientDTO(
                patient.getId(),
                patient.getName(),
                patient.getLastName(),
                patient.getSecondLastName(),
                patient.getGender(),
                patient.getApproximateAge(),
                patient.getRegistrationDateTime(),
                registeringUserFullName,
                patient.isActive(),
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



