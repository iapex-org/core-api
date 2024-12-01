package com.iapex.services;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iapex.dtos.MembershipDTO;
import com.iapex.exceptions.InstitutionAlreadyExistsException;
import com.iapex.exceptions.InstitutionNotFoundException;
import com.iapex.models.Membership;
import com.iapex.models.institution.Institution;
import com.iapex.models.response.Response;
import com.iapex.repositories.MembershipRepository;
import com.iapex.repositories.institution.InstitutionRepository;

@Service
public class MembershipService {

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Transactional
    public Response registerMembership(MembershipDTO request)
            throws InstitutionNotFoundException {

        // VERIFICAR SI LA INSTITUCIÓN EXISTE
        Institution institution = institutionRepository.findByName(request.getInstitutionName())
                .orElseThrow(() -> new InstitutionNotFoundException("La institución no existe"));

        // VALIDAR QUE LA FECHA DE FINALIZACIÓN SEA MAYOR A LA FECHA DE INICIO
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new IllegalArgumentException("La fecha de finalización debe ser posterior a la fecha de inicio.");
        }

        // VERIFICAR SI YA EXISTE UNA MEMBRESÍA ACTIVA PARA LA INSTITUCIÓN
        if (membershipRepository.findByInstitutionAndStatus(institution, true).isPresent()) {
            throw new IllegalArgumentException("Ya existe una membresía activa para esta institución.");
        }

        // CREAR NUEVA MEMBRESÍA Y ASIGNAR AUTOMÁTICAMENTE EL ESTADO ACTIVO
        Membership newMembership = new Membership();
        newMembership.setStartDate(request.getStartDate());
        newMembership.setEndDate(request.getEndDate());
        newMembership.setStatus(true); // Estado automáticamente activo
        newMembership.setInstitution(institution);

        // GUARDAR LA NUEVA MEMBRESÍA
        membershipRepository.save(newMembership);

        // ACTUALIZAR EL ESTADO DE LA INSTITUCIÓN COMO ACTIVO
        institution.setActive(true);
        institutionRepository.save(institution);

        return new Response("Membresía registrada y activada con éxito.");
    }

    @Transactional
    public Response updateById(Long id, MembershipDTO request) throws InstitutionNotFoundException {
        if (id == null || request == null)
            return new Response("ID o datos de la solicitud no válidos");

        Membership membership = membershipRepository.findById(id).orElse(null);

        if (membership == null)
            return new Response("Membresía no encontrada");

        // Verificación si la membresía está desactivada
        if (!membership.isStatus()) {
            throw new IllegalArgumentException("La membresía está desactivada y no puede ser actualizada");
        }

        if (request.getInstitutionName() == null)
            return new Response("Nombre de la institución no proporcionado");

        if (request.getStartDate() == null || request.getEndDate() == null)
            return new Response("Fechas de inicio o fin no proporcionadas");

        if (request.getEndDate().isBefore(request.getStartDate())
                || Objects.equals(request.getEndDate(), request.getStartDate()))
            return new Response("La fecha de finalización debe ser posterior a la fecha de inicio");

        // Actualizar la membresía
        membership.setStartDate(request.getStartDate());
        membership.setEndDate(request.getEndDate());

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
                membership.getInstitution().getName());
    }

    @Transactional
    public Response deleteMembership(Long id) {
        Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada"));

        // En lugar de eliminar, desactivamos la membresía
        membership.setStatus(false);

        // También actualizamos el estado de la institución
        Institution institution = membership.getInstitution();
        institution.setActive(false);

        // Guardamos los cambios
        institutionRepository.save(institution);
        membershipRepository.save(membership);

        return new Response("Membresía desactivada con éxito");
    }
}
