package com.iapex.services;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iapex.dtos.contactRequest.ContactRequestCount;
import com.iapex.dtos.contactRequest.ContactRequestDTO;
import com.iapex.dtos.contactRequest.UpdateContactRequestDTO;
import com.iapex.enums.ContactRequestStatusEnum;
import com.iapex.models.ContactRequest;
import com.iapex.models.institution.Institution;
import com.iapex.models.patient.Patient;
import com.iapex.models.response.Response;
import com.iapex.models.user.UserWeb;
import com.iapex.repositories.ContactRequestRepository;
import com.iapex.repositories.ContactRequestRepositoryImpl;
import com.iapex.repositories.PatientRepository;

@Service
public class ContactRequestService {

    @Autowired
    private ContactRequestRepository contactRequestRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ContactRequestRepositoryImpl contactRequestRepositoryImpl;

    /**
     * Obtiene el conteo de solicitudes de contacto agrupadas por estado y el total.
     * 
     * @return Map<String, Long> Mapa con el conteo por cada estado y el total
     */
    public Map<String, Long> getContactRequestStatusCount() {
        try {
            List<ContactRequestCount> statusCounts = contactRequestRepositoryImpl.getContactRequestStatusCount();
            Map<String, Long> result = new LinkedHashMap<>(); // LinkedHashMap para mantener el orden

            long total = 0;
            for (ContactRequestCount count : statusCounts) {
                result.put(count.getStatus(), count.getCount());
                total += count.getCount();
            }

            // Agregar el total al final del mapa
            result.put("total", total);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new LinkedHashMap<>();
        }
    }

    public Map<String, Long> getInstitutionStatistics() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        UserWeb currentUser = (UserWeb) authentication.getPrincipal();
        Institution institution = currentUser.getInstitution();
        if (institution == null) {
            throw new Exception("Usuario no tiene una institución asignada");
        }

        List<ContactRequestCount> statusCounts = contactRequestRepositoryImpl
                .getContactRequestStatusCountByInstitution(institution.getId());

        Map<String, Long> result = new LinkedHashMap<>();
        long total = 0;

        for (ContactRequestCount count : statusCounts) {
            result.put(count.getStatus(), count.getCount());
            total += count.getCount();
        }
        result.put("total", total);

        return result;
    }

    @Transactional
    public Response createContactRequest(ContactRequestDTO request) throws Exception {
        ContactRequest contactRequest = new ContactRequest();
        contactRequest.setInterestedPersonName(request.getInterestedPersonName());
        contactRequest.setMissingPersonName(request.getMissingPersonName());

        Patient patient = patientRepository.findById(request.getPatient())
                .orElseThrow(() -> new Exception("Paciente no encontrado con el ID proporcionado"));
        contactRequest.setPatient(patient);

        contactRequest.setPhoneNumber(request.getPhoneNumber());
        contactRequest.setEmail(request.getEmail());
        contactRequest.setRelationship(request.getRelationship());
        contactRequest.setRequestDateTime(LocalDateTime.now());
        contactRequest.setMessage(request.getMessage());
        contactRequest.setStatus("NUEVA");

        contactRequestRepository.save(contactRequest);
        return new Response("Solicitud de contacto enviada exitosamente");
    }

    public ContactRequestDTO getContactRequestById(Long id) throws Exception {
        ContactRequest contactRequest = contactRequestRepository.findById(id)
                .orElseThrow(() -> new Exception("Solicitud de contacto no encontrada"));
        return convertToDTO(contactRequest);
    }

    public List<ContactRequestDTO> getAllContactRequests() {
        List<ContactRequest> contactRequests = contactRequestRepository.findAll();
        return contactRequests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Response updateContactRequestById(Long id, UpdateContactRequestDTO request) {
        try {
            // Obtener la solicitud de contacto
            ContactRequest contactRequest = contactRequestRepository.findById(id)
                    .orElseThrow(() -> new Exception("Solicitud de contacto no encontrada"));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserWeb currentUser = (UserWeb) authentication.getPrincipal();

            boolean statusUpdated = false;
            boolean userUpdated = false;

            // Actualizar el estado si está presente en la solicitud
            if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
                ContactRequestStatusEnum newStatus;
                try {
                    newStatus = ContactRequestStatusEnum.valueOf(request.getStatus().toUpperCase().replace(" ", "_"));
                } catch (IllegalArgumentException e) {
                    return new Response("Error: El estado proporcionado no es válido");
                }

                // Actualizar solo si el nuevo estado es diferente al actual
                if (!contactRequest.getStatus().equals(newStatus.name())) {
                    contactRequest.setStatus(newStatus.name());
                    statusUpdated = true;

                    // Actualizar automáticamente el usuario atendiendo
                    contactRequest.setAttendingUser(currentUser);
                    userUpdated = true;
                }
            }

            // Actualizar el usuario atendiendo si está presente en la solicitud y no se
            // actualizó automáticamente
            if (!userUpdated && request.getAttendingUser() != null && !request.getAttendingUser().trim().isEmpty()) {
                // Verificar si el usuario tiene permiso para cambiar el usuario atendiendo
                if (!currentUser.getUsername().equals(request.getAttendingUser())) {
                    return new Response("Error: No tienes permiso para cambiar el usuario asociado a esta solicitud");
                }

                contactRequest.setAttendingUser(currentUser);
                userUpdated = true;
            }

            // Guardar la solicitud de contacto con los cambios realizados
            contactRequestRepository.save(contactRequest);

            // Determinar el mensaje de respuesta según las actualizaciones realizadas
            if (statusUpdated && userUpdated) {
                return new Response("Estado y usuario atendiendo actualizados exitosamente");
            } else if (statusUpdated) {
                return new Response("Estado de la solicitud de contacto actualizado exitosamente");
            } else if (userUpdated) {
                return new Response("Usuario atendiendo actualizado exitosamente");
            } else {
                return new Response("No se realizaron cambios en la solicitud de contacto");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof AccessDeniedException || e.getCause() instanceof AccessDeniedException) {
                return new Response("Debes estar logueado para acceder a este recurso");
            }
            return new Response("Error al actualizar la solicitud de contacto: " + e.getMessage());
        }
    }

    public List<ContactRequestDTO> getContactRequestsByInstitution() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserWeb userWeb = (UserWeb) authentication.getPrincipal();
            Institution institution = userWeb.getInstitution();
            List<ContactRequest> contactRequests = contactRequestRepository.findByPatientInstitution(institution);
            return contactRequests.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private ContactRequestDTO convertToDTO(ContactRequest contactRequest) {
        Patient patient = contactRequest.getPatient();
        return new ContactRequestDTO(
                contactRequest.getId(),
                contactRequest.getInterestedPersonName(),
                contactRequest.getAttendingUser() != null ? String.format("%s %s %s",
                        contactRequest.getAttendingUser().getName(),
                        contactRequest.getAttendingUser().getLastName(),
                        contactRequest.getAttendingUser().getSecondLastName()).trim() : null,
                contactRequest.getMissingPersonName(),
                patient.getId(),
                contactRequest.getPhoneNumber(),
                contactRequest.getEmail(),
                contactRequest.getRelationship(),
                contactRequest.getRequestDateTime(),
                contactRequest.getMessage(),
                contactRequest.getStatus());
    }
}