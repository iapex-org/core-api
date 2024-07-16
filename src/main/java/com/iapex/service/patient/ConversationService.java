package com.iapex.service.patient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iapex.dto.patient.ConversationDTO;
import com.iapex.model.institution.Institution;
import com.iapex.model.patient.Conversation;
import com.iapex.model.patient.Patient;
import com.iapex.model.response.Response;
import com.iapex.model.user.UserInstitution;
import com.iapex.repository.patient.ConversationRepository;
import com.iapex.repository.patient.PatientRepository;

@Service
public class ConversationService {
    
	@Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private PatientRepository patientRepository;

    // REGISTRA UNA NUEVA CONVERSACIÓN
    @Transactional
    public Response registerConversation(ConversationDTO request) {
        try {
            Conversation conversation = new Conversation();
            conversation.setInterestedName(request.getInterestedName());
            conversation.setSearcherName(request.getSearcherName());
            
            if (request.getIdPatient() != null) {
                Patient patient = patientRepository.findById(request.getIdPatient())
                    .orElseThrow(() -> new Exception("Paciente no encontrado con el ID proporcionado"));
                conversation.setPatient(patient);
            } else {
                throw new Exception("Se debe proporcionar el ID del paciente");
            }

            conversation.setPhoneNumber(request.getPhoneNumber());
            conversation.setEmail(request.getEmail());
            conversation.setPatientRelationship(request.getPatientRelationship());
            conversation.setRequestDate(LocalDateTime.now());
            conversation.setMessage(request.getMessage());
            conversation.setStatus("Nueva");

            conversationRepository.save(conversation);
            return new Response("Solicitud de contacto enviada exitosamente");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("Error al registrar la solicitud de contacto: " + e.getMessage());
        }
    }

    // OBTIENE UNA CONVERSACIÓN POR SU ID
    public ConversationDTO getConversationById(Long id) throws Exception {
        Conversation conversation = conversationRepository.findById(id)
            .orElseThrow(() -> new Exception("Conversación no encontrada"));
        return convertToDTO(conversation);
    }
    

    // OBTIENE TODAS LAS CONVERSACIONES
    public List<ConversationDTO> getAllConversations() {
        List<Conversation> conversations = conversationRepository.findAll();
        return conversations.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    
    @Transactional
    public Response updateById(Long id, ConversationDTO request, String name, String fatherName, String motherName) {
        try {
            // VERIFICAR SI SE PROPORCIONÓ UN NUEVO ESTADO
            if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
                return new Response("Error: Se debe proporcionar un nuevo estado para la conversación");}
            // BUSCAR LA CONVERSACIÓN POR SU ID O LANZAR UNA EXCEPCIÓN SI NO SE ENCUENTRA
            Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new Exception("Conversación no encontrada"));
            // ACTUALIZAR EL ESTADO DE LA CONVERSACIÓN SI ES DIFERENTE
            if (!Objects.equals(conversation.getStatus(), request.getStatus())) {
                conversation.setStatus(request.getStatus());
                // CREAR EL NOMBRE COMPLETO DE LA PERSONA QUE ATIENDE
                String fullName = String.format("%s %s %s", name, fatherName, motherName).trim();
                conversation.setAttendeddBy(fullName);
                // GUARDAR LOS CAMBIOS
                conversationRepository.save(conversation);
                return new Response("Estado de la conversación actualizado exitosamente");
            } else {
                return new Response("El estado proporcionado es igual al estado actual. No se realizaron cambios.");            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("Error al actualizar el estado de la conversación: " + e.getMessage()); } }
    
    
    // OBTIENE LAS CONVERSACIONES DE LA MISMA INSTITUCIÓN QUE EL USUARIO AUTENTICADO
    public List<ConversationDTO> getConversationsForAuthenticatedUser() {
        try {
            // OBTENER LA INFORMACIÓN DEL USUARIO AUTENTICADO
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserInstitution userInstitution = (UserInstitution) authentication.getPrincipal();
            // OBTENER LA INSTITUCIÓN DEL USUARIO AUTENTICADO
            Institution institution = userInstitution.getInstitution();
            // OBTENER LAS CONVERSACIONES ASOCIADAS A LA INSTITUCIÓN DEL USUARIO AUTENTICADO
            List<Conversation> conversations = conversationRepository.findByPatientInstitution(institution);
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
    



    private ConversationDTO convertToDTO(Conversation conversation) {
        Patient patient = conversation.getPatient();
        String patientName = "";
        if (patient != null) {
            patientName = String.format("%s %s %s", 
                patient.getName() != null ? patient.getName() : "",
                patient.getFathername() != null ? patient.getFathername() : "",
                patient.getMothername() != null ? patient.getMothername() : "")
                .trim().replaceAll("\\s+", " ");
        }
        
        return new ConversationDTO(
            conversation.getIdConversation(),
            conversation.getInterestedName(),
            conversation.getAttendeddBy(),
            conversation.getSearcherName(),
            patient != null ? patient.getIdPatient() : null,
            patientName.isEmpty() ? null : patientName,
            conversation.getPhoneNumber(),
            conversation.getEmail(),
            conversation.getPatientRelationship(),
            conversation.getRequestDate(),
            conversation.getMessage(),
            conversation.getStatus()
        );
    }
}

