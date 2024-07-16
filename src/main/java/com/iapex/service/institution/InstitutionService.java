package com.iapex.service.institution;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.iapex.dto.institution.InstitutionDTO;
import com.iapex.exceptions.InstitutionAlreadyExistsException;
import com.iapex.model.institution.Contact;
import com.iapex.model.institution.Direction;
import com.iapex.model.institution.Institution;
import com.iapex.model.response.Response;
import com.iapex.model.userWeb.UserInstitution;
import com.iapex.repository.institution.InstitutionRepository;
import com.iapex.service.email.WebEmailService;

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
        //if (institutionRepository.findByEmail(request.getEmail()).isPresent()) {
            //throw new InstitutionAlreadyExistsException("Ya existe una institución registrada con este correo electrónico.");}
        
        if (institutionRepository.findByName(request.getName()).isPresent()) {
            throw new InstitutionAlreadyExistsException("Ya existe una institución registrada con este nombre.");
        }

        Institution institution = new Institution();
        institution.setName(request.getName());
        institution.setEmail(request.getEmail());
        institution.setTypeInstitution(request.getTypeInstitution());
        institution.setOpeningHours(request.getOpeningHours());
        institution.setHistory(request.getHistory());
        institution.setImage(request.getImage());
        institution.setImageUrl(request.getImageUrl());
        
        
        // CONFIGURAR CONTACT
        Contact contact = new Contact();
        contact.setPhone(request.getContactPhone());
        contact.setWebsite(request.getContactWebsite());
        institution.setContact(contact);
        
        // CONFIGURAR DIRECTION
        Direction direction = new Direction();
        direction.setUrlMapsInstitution(request.getDirectionUrlMapsInstitution());
        direction.setState(request.getDirectionState());
        direction.setMunicipality(request.getDirectionMunicipality());
        direction.setPostalCode(request.getDirectionPostalCode());
        direction.setColony(request.getDirectionColony());
        direction.setStreet(request.getDirectionStreet());
        direction.setNumber(request.getDirectionNumber());
        institution.setDirection(direction);

        institution.setStatus(false); // INSTITUCIÓN NO VERIFICADA INICIALMENTE

        // ESTABLECER LA FECHA DE REGISTRO A LA FECHA ACTUAL
        institution.setRegistrationDate(new Date());

        institutionRepository.save(institution);

        return new Response("El registro fue exitoso. Recuerda que debes habilitar la institución una vez que la hayas registrado.");
    }
    

    //ACTUALIZAR POR ID
    public Response updateInstitution(Long id, InstitutionDTO request) throws Exception {
        Institution institution = getInstitutionById(id);

        if (!Objects.equals(institution.getName(), request.getName()) && institutionRepository.findByName(request.getName()).isPresent()) throw new InstitutionAlreadyExistsException("Ya existe una institución registrada con este nombre.");

        if (!Objects.equals(institution.getName(), request.getName())) institution.setName(request.getName());
        if (!Objects.equals(institution.getEmail(), request.getEmail())) institution.setEmail(request.getEmail());
        if (!Objects.equals(institution.getTypeInstitution(), request.getTypeInstitution())) institution.setTypeInstitution(request.getTypeInstitution());
        if (!Objects.equals(institution.getOpeningHours(), request.getOpeningHours())) institution.setOpeningHours(request.getOpeningHours());
        if (!Objects.equals(institution.getHistory(), request.getHistory())) institution.setHistory(request.getHistory());
        if (!Objects.equals(institution.getImage(), request.getImage())) institution.setImage(request.getImage());
        if (!Objects.equals(institution.getImageUrl(), request.getImageUrl())) institution.setImageUrl(request.getImageUrl());
        if (institution.isStatus() != request.isStatus()) institution.setStatus(request.isStatus());

        Contact contact = institution.getContact();
        if (!Objects.equals(contact.getPhone(), request.getContactPhone())) contact.setPhone(request.getContactPhone());
        if (!Objects.equals(contact.getWebsite(), request.getContactWebsite())) contact.setWebsite(request.getContactWebsite());

        Direction direction = institution.getDirection();
        if (!Objects.equals(direction.getUrlMapsInstitution(), request.getDirectionUrlMapsInstitution())) direction.setUrlMapsInstitution(request.getDirectionUrlMapsInstitution());
        if (!Objects.equals(direction.getState(), request.getDirectionState())) direction.setState(request.getDirectionState());
        if (!Objects.equals(direction.getMunicipality(), request.getDirectionMunicipality())) direction.setMunicipality(request.getDirectionMunicipality());
        if (!Objects.equals(direction.getPostalCode(), request.getDirectionPostalCode())) direction.setPostalCode(request.getDirectionPostalCode());
        if (!Objects.equals(direction.getColony(), request.getDirectionColony())) direction.setColony(request.getDirectionColony());
        if (!Objects.equals(direction.getStreet(), request.getDirectionStreet())) direction.setStreet(request.getDirectionStreet());
        if (!Objects.equals(direction.getNumber(), request.getDirectionNumber())) direction.setNumber(request.getDirectionNumber());

        institutionRepository.save(institution);

        return new Response("La institución ha sido actualizada exitosamente.");
    }
    
    //OBTENER POR ID
    public Institution getInstitutionById(Long id) throws Exception {
        return institutionRepository.findById(id)
            .orElseThrow(() -> new Exception("Institución no encontrada "));
    }
    
    //OBTENER POR ID SOLO SI STATUS ES TRUE
    public Institution getInstitutionByIdTrue(Long id) throws Exception {
        return institutionRepository.findByIdInstitutionAndStatusTrue(id)
            .orElseThrow(() -> new Exception("Institución no encontrada o no está activa"));
    }

    //LISTAR INSTITUCIONES
    public List<InstitutionDTO> getAllInstitutions() {
        List<Institution> institutions = institutionRepository.findAll();
        return institutions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    //LISTAR INSTITUCIONES CON STATUS TRUE
    public List<InstitutionDTO> getAllInstitutionsTrue() {
        List<Institution> institutions = institutionRepository.findByStatusTrue();
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
    
    //BUSCAR POR NOMBRE CON STATUS TRUE
    public Institution getInstitutionByName(String name) throws Exception {
        return institutionRepository.findByNameAndStatusTrue(name)
            .orElseThrow(() -> new Exception("Institución no encontrada con nombre: " + name));
    }
    
    // MÉTODO PARA OBTENER LA INSTITUCIÓN DTO POR USUARIO
    public InstitutionDTO getInstitutionDTOByUser(UserInstitution userInstitution) throws Exception {
        Institution institution = userInstitution.getInstitution();
        if (institution == null) {
            throw new Exception("Institución no encontrada para el usuario autenticado");
        }
        return convertToDto(institution);
    }

    
    public InstitutionDTO convertToDto(Institution institution) {
        InstitutionDTO dto = new InstitutionDTO();
        dto.setIdInstitution(institution.getIdInstitution());
        dto.setName(institution.getName());
        dto.setEmail(institution.getEmail());
        dto.setTypeInstitution(institution.getTypeInstitution());
        dto.setOpeningHours(institution.getOpeningHours());
        dto.setHistory(institution.getHistory());
        dto.setImageUrl(institution.getImageUrl());
        dto.setStatus(institution.isStatus());
        dto.setRegistrationDate(institution.getRegistrationDate());

        if (institution.getContact() != null) {
            dto.setContactPhone(institution.getContact().getPhone());
        }

        if (institution.getDirection() != null) {
            dto.setDirectionUrlMapsInstitution(institution.getDirection().getUrlMapsInstitution());
            dto.setDirectionState(institution.getDirection().getState());
            dto.setDirectionMunicipality(institution.getDirection().getMunicipality());
            dto.setDirectionPostalCode(institution.getDirection().getPostalCode());
            dto.setDirectionColony(institution.getDirection().getColony());
            dto.setDirectionStreet(institution.getDirection().getStreet());
            dto.setDirectionNumber(institution.getDirection().getNumber());
        }

        return dto;
    }
}

