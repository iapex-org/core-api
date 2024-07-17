package com.iapex.service.membership;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iapex.dto.membership.MembershipDTO;
import com.iapex.exceptions.InstitutionAlreadyExistsException;
import com.iapex.exceptions.InstitutionNotFoundException;
import com.iapex.model.institution.Institution;
import com.iapex.model.membership.Membership;
import com.iapex.model.response.Response;
import com.iapex.repository.institution.InstitutionRepository;
import com.iapex.repository.membership.MembershipRepository;

@Service
public class MembershipService {

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private InstitutionRepository institutionRepository;
    
    
    @Transactional
    public Response registerMembership(MembershipDTO request) throws InstitutionNotFoundException, InstitutionAlreadyExistsException {
        // VERIFICAR SI LA INSTITUCIÓN EXISTE
        Institution institution = institutionRepository.findByName(request.getInstitutionName())
            .orElseThrow(() -> new InstitutionNotFoundException("La institución no existe"));

        // VALIDAR QUE LA FECHA DE FINALIZACIÓN SEA MAYOR A LA FECHA DE INICIO
        if (request.getEndDate().isBefore(request.getStartDate()) || request.getEndDate().isEqual(request.getStartDate())) {
            throw new IllegalArgumentException("La fecha de finalización debe ser posterior a la fecha de inicio"); }

        // VERIFICAR SI YA EXISTE UNA MEMBRESÍA ACTIVA PARA LA INSTITUCIÓN
        if (membershipRepository.findByInstitutionAndStatus(institution, true).isPresent()) {
            throw new InstitutionAlreadyExistsException("Ya existe una membresía activa para esta institución"); }
        
        // CREAR NUEVA MEMBRESÍA
        Membership newMembership = new Membership();
        newMembership.setStartDate(request.getStartDate());
        newMembership.setEndDate(request.getEndDate());
        newMembership.setStatus(request.isStatus());
        newMembership.setInstitution(institution);

        // GUARDAR LA NUEVA MEMBRESÍA
        membershipRepository.save(newMembership);
        // ACTUALIZAR EL ESTADO DE LA INSTITUCIÓN A TRUE SI EL STATUS DE LA MEMBRESÍA ES TRUE
        if (request.isStatus()) {
            institution.setStatus(true);
            institutionRepository.save(institution);
        }
        return new Response("Membresía registrada con éxito, recuerda activar la membresía si no lo hiciste al registrarla");
    }
    
    
    @Transactional
    public Response updateById(Long id, MembershipDTO request) throws InstitutionNotFoundException {
        if (id == null || request == null) return new Response("ID o datos de la solicitud no válidos");

        Membership membership = membershipRepository.findById(id).orElse(null);

        if (membership == null) return new Response("Membresía no encontrada");

        if (request.getInstitutionName() == null) return new Response("Nombre de la institución no proporcionado");

        Institution institution = institutionRepository.findByName(request.getInstitutionName())
                .orElseThrow(() -> new InstitutionNotFoundException("La institución no existe"));

        if (request.getStartDate() == null || request.getEndDate() == null) return new Response("Fechas de inicio o fin no proporcionadas");
        if (request.getEndDate().isBefore(request.getStartDate()) || Objects.equals(request.getEndDate(), request.getStartDate())) 
            return new Response("La fecha de finalización debe ser posterior a la fecha de inicio");

        membership.setStartDate(request.getStartDate());
        membership.setEndDate(request.getEndDate());
        
        // GUARDAR EL ESTADO ANTERIOR DE LA MEMBRESÍA
        boolean previousStatus = membership.isStatus();
        membership.setStatus(request.isStatus());
        if (membership.getInstitution() == null || !Objects.equals(membership.getInstitution().getName(), request.getInstitutionName())) 
            membership.setInstitution(institution);

        // ACTUALIZAR EL ESTADO DE LA INSTITUCIÓN SI EL ESTADO DE LA MEMBRESÍA HA CAMBIADO
        if (previousStatus != request.isStatus()) {
            institution.setStatus(request.isStatus());
            institutionRepository.save(institution);
        }
        membershipRepository.save(membership);
        return new Response("Membresía actualizada con éxito");
    }
    
    
    // OBTENER MEMEBRECIA POR ID
    public MembershipDTO getMembershipById(Long id) throws Exception {
        Membership membership = membershipRepository.findById(id)
            .orElseThrow(() -> new Exception("Membresía no encontrada"));
        return convertToDTO(membership);
    }
    
    
    // OBTENER TODAS LAS MENBRECIAS
    public List<MembershipDTO> getAllMemberships() {
        List<Membership> memberships = membershipRepository.findAll();
        return memberships.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    

    private MembershipDTO convertToDTO(Membership membership) {
        return new MembershipDTO(
            membership.getId(),
            membership.getStartDate(),
            membership.getEndDate(),
            membership.isStatus(),
            membership.getInstitution().getName()
        );
    }
}