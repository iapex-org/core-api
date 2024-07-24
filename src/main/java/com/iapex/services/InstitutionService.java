package com.iapex.services;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.iapex.dtos.InstitutionDTO;
import com.iapex.exceptions.InstitutionAlreadyExistsException;
import com.iapex.models.institution.Direction;
import com.iapex.models.institution.Institution;
import com.iapex.models.response.Response;
import com.iapex.models.user.UserWeb;
import com.iapex.repositories.institution.InstitutionRepository;
import com.iapex.services.email.WebEmailService;

@Service
public class InstitutionService {

    @Autowired
    private InstitutionRepository institutionRepository;

    public InstitutionService(
            InstitutionRepository institutionRepository,
            PasswordEncoder passwordEncoder,
            WebEmailService webEmailService) {
        this.institutionRepository = institutionRepository;
    }
    
    //CREAR UNA INSTITUCION
    public Response register(InstitutionDTO request) throws Exception {
        if (institutionRepository.findByName(request.getName()).isPresent()) {
            throw new InstitutionAlreadyExistsException("Ya existe una institución registrada con este nombre.");
        }

        Institution institution = new Institution();
        institution.setName(request.getName());
        institution.setOpeningHours(request.getOpeningHours());
        institution.setMapUrl(request.getMapUrl());
        institution.setEmails(request.getEmails());
        institution.setImage(request.getImage());
        institution.setImageUrl(request.getImageUrl());
        institution.setType(request.getType());
        institution.setPhoneNumbers(request.getPhoneNumbers());
        institution.setWebsites(request.getWebsites());
        institution.setVerificationKey(request.getVerificationKey());
        
        // CONFIGURAR DIRECTION
        Direction direction = new Direction();
        direction.setState(request.getState());
        direction.setCity(request.getCity());
        direction.setPostalCode(request.getPostalCode());
        direction.setNeighborhood(request.getNeighborhood());
        direction.setStreet(request.getStreet());
        direction.setNumber(request.getNumber());
        institution.setDirection(direction);

        institution.setActive(false); // INSTITUCIÓN NO VERIFICADA INICIALMENTE

        // ESTABLECER LA FECHA DE REGISTRO A LA FECHA ACTUAL
        institution.setRegistrationDateTime(new Date());

        institutionRepository.save(institution);

        return new Response("El registro fue exitoso. Recuerda que debes habilitar la institución una vez que la hayas registrado.");
    }
    

    //ACTUALIZAR POR ID
    public Response updateInstitution(Long id, InstitutionDTO request) throws Exception {
        Institution institution = getInstitutionById(id);

        if (!Objects.equals(institution.getName(), request.getName()) && institutionRepository.findByName(request.getName()).isPresent()) 
            throw new InstitutionAlreadyExistsException("Ya existe una institución registrada con este nombre.");

        if (!Objects.equals(institution.getName(), request.getName())) institution.setName(request.getName());
        if (!Objects.equals(institution.getOpeningHours(), request.getOpeningHours())) institution.setOpeningHours(request.getOpeningHours());
        if (!Objects.equals(institution.getMapUrl(), request.getMapUrl())) institution.setMapUrl(request.getMapUrl());
        if (!Objects.equals(institution.getEmails(), request.getEmails())) institution.setEmails(request.getEmails());
        if (!Objects.equals(institution.getImage(), request.getImage())) institution.setImage(request.getImage());
        if (!Objects.equals(institution.getImageUrl(), request.getImageUrl())) institution.setImageUrl(request.getImageUrl());
        if (!Objects.equals(institution.getType(), request.getType())) institution.setType(request.getType());
        if (!Objects.equals(institution.getPhoneNumbers(), request.getPhoneNumbers())) institution.setPhoneNumbers(request.getPhoneNumbers());
        if (!Objects.equals(institution.getWebsites(), request.getWebsites())) institution.setWebsites(request.getWebsites());
        if (!Objects.equals(institution.getVerificationKey(), request.getVerificationKey())) institution.setVerificationKey(request.getVerificationKey());
        if (institution.isActive() != request.isActive()) institution.setActive(request.isActive());

        Direction direction = institution.getDirection();
        if (!Objects.equals(direction.getState(), request.getState())) direction.setState(request.getState());
        if (!Objects.equals(direction.getCity(), request.getCity())) direction.setCity(request.getCity());
        if (!Objects.equals(direction.getPostalCode(), request.getPostalCode())) direction.setPostalCode(request.getPostalCode());
        if (!Objects.equals(direction.getNeighborhood(), request.getNeighborhood())) direction.setNeighborhood(request.getNeighborhood());
        if (!Objects.equals(direction.getStreet(), request.getStreet())) direction.setStreet(request.getStreet());
        if (!Objects.equals(direction.getNumber(), request.getNumber())) direction.setNumber(request.getNumber());

        institutionRepository.save(institution);

        return new Response("La institución ha sido actualizada exitosamente.");
    }
    
    //OBTENER POR ID
    public Institution getInstitutionById(Long id) throws Exception {
        return institutionRepository.findById(id)
            .orElseThrow(() -> new Exception("Institución no encontrada "));
    }
    
    //OBTENER POR ID SOLO SI ACTIVE ES TRUE
    public Institution getInstitutionByIdTrue(Long id) throws Exception {
        return institutionRepository.findByIdAndActiveTrue(id)
            .orElseThrow(() -> new Exception("Institución no encontrada o no está activa"));
    }

    //LISTAR INSTITUCIONES
    public List<InstitutionDTO> getAllInstitutions() {
        List<Institution> institutions = institutionRepository.findAll();
        return institutions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    //LISTAR INSTITUCIONES CON ACTIVE TRUE
    public List<InstitutionDTO> getAllInstitutionsTrue() {
        List<Institution> institutions = institutionRepository.findByActiveTrue();
        return institutions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    
    //ELIMINAR POR ID
    public Response deleteInstitution(Long id) throws Exception {
        Institution institution = getInstitutionById(id);
        institutionRepository.delete(institution);
        return new Response("La institución ha sido eliminada exitosamente.");
    }
    
    //BUSCAR POR NOMBRE CON ACTIVE TRUE
    public Institution getInstitutionByName(String name) throws Exception {
        return institutionRepository.findByNameAndActiveTrue(name)
            .orElseThrow(() -> new Exception("Institución no encontrada con nombre: " + name));
    }
    
    // MÉTODO PARA OBTENER LA INSTITUCIÓN DTO POR USUARIO
    public InstitutionDTO getInstitutionDTOByUser(UserWeb userWeb) throws Exception {
        Institution institution = userWeb.getInstitution();
        if (institution == null) {
            throw new Exception("Institución no encontrada para el usuario autenticado");
        }
        return convertToDto(institution);
    }

    public List<String> getActiveInstitutionNames() {
        List<Institution> activeInstitutions = institutionRepository.findByActiveTrue();
        return activeInstitutions.stream()
                .map(Institution::getName)
                .collect(Collectors.toList());
    }
    
    public InstitutionDTO convertToDto(Institution institution) {
        InstitutionDTO dto = new InstitutionDTO();
        dto.setId(institution.getId());
        dto.setName(institution.getName());
        dto.setOpeningHours(institution.getOpeningHours());
        dto.setMapUrl(institution.getMapUrl());
        dto.setEmails(institution.getEmails());
        dto.setImage(institution.getImage());
        dto.setImageUrl(institution.getImageUrl());
        dto.setType(institution.getType());
        dto.setPhoneNumbers(institution.getPhoneNumbers());
        dto.setWebsites(institution.getWebsites());
        dto.setVerificationKey(institution.getVerificationKey());
        dto.setActive(institution.isActive());
        dto.setRegistrationDateTime(institution.getRegistrationDateTime());

        if (institution.getDirection() != null) {
            dto.setState(institution.getDirection().getState());
            dto.setCity(institution.getDirection().getCity());
            dto.setPostalCode(institution.getDirection().getPostalCode());
            dto.setNeighborhood(institution.getDirection().getNeighborhood());
            dto.setStreet(institution.getDirection().getStreet());
            dto.setNumber(institution.getDirection().getNumber());
        }

        return dto;
    }
}


