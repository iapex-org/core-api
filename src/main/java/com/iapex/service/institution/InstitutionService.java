package com.iapex.service.institution;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.iapex.exceptions.InstitutionAlreadyExistsException;
import com.iapex.institution.DTO.InstitutionDTO;
import com.iapex.model.Response;
import com.iapex.model.institution.Institution;
import com.iapex.model.institution.Contact;
import com.iapex.model.institution.Marker;
import com.iapex.repository.InstitutionRepository;
import com.iapex.service.mail.EmailService;

@Service
public class InstitutionService {

    @Autowired
    private InstitutionRepository institutionRepository;


    public InstitutionService(
            InstitutionRepository institutionRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.institutionRepository = institutionRepository;
    }
    
    //CREAR UNA INSTITUCION
    public Response register(InstitutionDTO request) throws Exception {
        if (institutionRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new InstitutionAlreadyExistsException("Ya existe una institución registrada con este correo electrónico.");
        }
        
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
        contact.setAdress(request.getContactAddress());
        contact.setWebsite(request.getContactWebsite());
        institution.setContact(contact);
        
        // CONFIGURAR MARKER
        Marker marker = new Marker();
        marker.setCoordinates(request.getMarkerCoordinates());
        marker.setNameLoc(request.getMarkerNameLoc());
        institution.setMarker(marker);

        institution.setStatus(false); // INSTITUCIÓN NO VERIFICADA INICIALMENTE

        institutionRepository.save(institution);

        return new Response("El registro fue exitoso. Recuerda que debes habilitar la institución una vez que la hayas registrado.");
    }
    

    //ACTUALIZAR POR ID
    public Response updateInstitution(Long id, InstitutionDTO request) throws Exception {
        Institution institution = getInstitutionById(id);

        if (!institution.getEmail().equals(request.getEmail()) && institutionRepository.findByEmail(request.getEmail()).isPresent())
            throw new InstitutionAlreadyExistsException("Ya existe una institución registrada con este correo electrónico.");

        if (!institution.getName().equals(request.getName()) && institutionRepository.findByName(request.getName()).isPresent())
            throw new InstitutionAlreadyExistsException("Ya existe una institución registrada con este nombre.");

        if (!institution.getName().equals(request.getName())) institution.setName(request.getName());
        if (!institution.getEmail().equals(request.getEmail())) institution.setEmail(request.getEmail());
        if (!institution.getTypeInstitution().equals(request.getTypeInstitution())) institution.setTypeInstitution(request.getTypeInstitution());
        if (!institution.getOpeningHours().equals(request.getOpeningHours())) institution.setOpeningHours(request.getOpeningHours());
        if (!institution.getHistory().equals(request.getHistory())) institution.setHistory(request.getHistory());
        if (!institution.getImage().equals(request.getImage())) institution.setImage(request.getImage());
        if (!institution.getImageUrl().equals(request.getImageUrl())) institution.setImageUrl(request.getImageUrl());
        if (institution.isStatus() != request.isStatus()) institution.setStatus(request.isStatus());
        
        Contact contact = institution.getContact();
        if (!contact.getPhone().equals(request.getContactPhone())) contact.setPhone(request.getContactPhone());
        if (!contact.getAdress().equals(request.getContactAddress())) contact.setAdress(request.getContactAddress());
        if (!contact.getWebsite().equals(request.getContactWebsite())) contact.setWebsite(request.getContactWebsite());

        Marker marker = institution.getMarker();
        if (!marker.getCoordinates().equals(request.getMarkerCoordinates())) marker.setCoordinates(request.getMarkerCoordinates());
        if (!marker.getNameLoc().equals(request.getMarkerNameLoc())) marker.setNameLoc(request.getMarkerNameLoc());

        institutionRepository.save(institution);

        return new Response("La institución ha sido actualizada exitosamente.");
    }
    
    
    //OBTENER POR ID
    public Institution getInstitutionById(Long id) throws Exception {
        return institutionRepository.findById(id)
            .orElseThrow(() -> new Exception("Institución no encontrada "));
    }

    //LISTAR INSTITUCIONES
    public List<Institution> getAllInstitutions() {
        return institutionRepository.findAll();
    }

    //ELIMINAR POR ID
    public Response deleteInstitution(Long id) throws Exception {
        Institution institution = getInstitutionById(id);
        institutionRepository.delete(institution);
        return new Response("La institución ha sido eliminada exitosamente.");
    }
    
    //BUSCAR POR NOMBRE
    public Institution getInstitutionByName(String name) throws Exception {
        return institutionRepository.findByName(name)
            .orElseThrow(() -> new Exception("Institución no encontrada con nombre: " + name));
    }
    
    
}