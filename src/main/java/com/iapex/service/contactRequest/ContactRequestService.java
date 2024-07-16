package com.iapex.service.contactRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iapex.dto.contactRequest.ContactRequestDTO;
import com.iapex.model.contactRequest.ContactRequest;
import com.iapex.model.institution.Institution;
import com.iapex.model.patient.Patient;
import com.iapex.model.response.Response;
import com.iapex.model.userWeb.UserInstitution;
import com.iapex.repository.contactRequest.ContactRequestRepository;
import com.iapex.repository.patient.PatientRepository;

@Service
public class ContactRequestService  {
    
	@Autowired
    private ContactRequestRepository contactRequestRepository;

    @Autowired
    private PatientRepository patientRepository;

    // REGISTRA UNA NUEVA CONVERSACIÓN
    @Transactional
    public Response registerConversation(ContactRequestDTO request) {
        try {
        	ContactRequest contactRequest = new ContactRequest();
        	contactRequest.setInterestedName(request.getInterestedName());
            contactRequest.setSearcherName(request.getSearcherName());
            
            if (request.getIdPatient() != null) {
                Patient patient = patientRepository.findById(request.getIdPatient())
                    .orElseThrow(() -> new Exception("Paciente no encontrado con el ID proporcionado"));
                contactRequest.setPatient(patient);
            } else {
                throw new Exception("Se debe proporcionar el ID del paciente");
            }

            contactRequest.setPhoneNumber(request.getPhoneNumber());
            contactRequest.setEmail(request.getEmail());
            contactRequest.setPatientRelationship(request.getPatientRelationship());
            contactRequest.setRequestDate(LocalDateTime.now());
            contactRequest.setMessage(request.getMessage());
            contactRequest.setStatus("Nueva");

            contactRequestRepository.save(contactRequest);
            return new Response("Solicitud de contacto enviada exitosamente");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("Error al registrar la solicitud de contacto: " + e.getMessage());
        }
    }

    // OBTIENE UNA CONVERSACIÓN POR SU ID
    public ContactRequestDTO getConversationById(Long id) throws Exception {
    	ContactRequest contactRequest = contactRequestRepository.findById(id)
            .orElseThrow(() -> new Exception("Conversación no encontrada"));
        return convertToDTO(contactRequest);
    }
    

    // OBTIENE TODAS LAS CONVERSACIONES
    public List<ContactRequestDTO> getAllConversations() {
        List<ContactRequest> conversations = contactRequestRepository.findAll();
        return conversations.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    
    @Transactional
    public Response updateById(Long id, ContactRequestDTO request, String name, String fatherName, String motherName) {
        try {
            // VERIFICAR SI SE PROPORCIONÓ UN NUEVO ESTADO
            if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
                return new Response("Error: Se debe proporcionar un nuevo estado para la conversación");}
            // BUSCAR LA CONVERSACIÓN POR SU ID O LANZAR UNA EXCEPCIÓN SI NO SE ENCUENTRA
            ContactRequest contactRequest = contactRequestRepository.findById(id)
                .orElseThrow(() -> new Exception("Conversación no encontrada"));
            // ACTUALIZAR EL ESTADO DE LA CONVERSACIÓN SI ES DIFERENTE
            if (!Objects.equals(contactRequest.getStatus(), request.getStatus())) {
            	contactRequest.setStatus(request.getStatus());
                // CREAR EL NOMBRE COMPLETO DE LA PERSONA QUE ATIENDE
                String fullName = String.format("%s %s %s", name, fatherName, motherName).trim();
                contactRequest.setAttendedBy(fullName);
                // GUARDAR LOS CAMBIOS
                contactRequestRepository.save(contactRequest);
                return new Response("Estado de la conversación actualizado exitosamente");
            } else {
                return new Response("El estado proporcionado es igual al estado actual. No se realizaron cambios.");            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("Error al actualizar el estado de la conversación: " + e.getMessage()); } }
    
    
    // OBTIENE LAS CONVERSACIONES DE LA MISMA INSTITUCIÓN QUE EL USUARIO AUTENTICADO
    public List<ContactRequestDTO> getConversationsForAuthenticatedUser() {
        try {
            // OBTENER LA INFORMACIÓN DEL USUARIO AUTENTICADO
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserInstitution userInstitution = (UserInstitution) authentication.getPrincipal();
            // OBTENER LA INSTITUCIÓN DEL USUARIO AUTENTICADO
            Institution institution = userInstitution.getInstitution();
            // OBTENER LAS CONVERSACIONES ASOCIADAS A LA INSTITUCIÓN DEL USUARIO AUTENTICADO
            List<ContactRequest> conversations = contactRequestRepository.findByPatientInstitution(institution);
            // CONVERTIR LAS CONVERSACIONES A DTOS
            return conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            // MANEJAR LA EXCEPCIÓN ADECUADAMENTE SEGÚN TUS REQUERIMIENTOS
            return Collections.emptyList(); // O PODRÍAS RETORNAR UN MENSAJE DE ERROR
        }
    }
    



    private ContactRequestDTO convertToDTO(ContactRequest contactRequest) {
        Patient patient = contactRequest.getPatient();
        String patientName = "";
        if (patient != null) {
            patientName = String.format("%s %s %s", 
                patient.getName() != null ? patient.getName() : "",
                patient.getFathername() != null ? patient.getFathername() : "",
                patient.getMothername() != null ? patient.getMothername() : "")
                .trim().replaceAll("\\s+", " ");
        }
        
        return new ContactRequestDTO(
        		contactRequest.getIdContactRequest(),
        		contactRequest.getInterestedName(),
        		contactRequest.getAttendedBy(),
        		contactRequest.getSearcherName(),
            patient != null ? patient.getIdPatient() : null,
            patientName.isEmpty() ? null : patientName,
            		contactRequest.getPhoneNumber(),
            		contactRequest.getEmail(),
            		contactRequest.getPatientRelationship(),
            		contactRequest.getRequestDate(),
            		contactRequest.getMessage(),
            		contactRequest.getStatus()
        );
    }
}

