package com.iapex.service.patient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iapex.dto.patient.ConversationDTO;
import com.iapex.model.patient.Conversation;
import com.iapex.model.response.Response;
import com.iapex.repository.patient.ConversationRepository;

@Service
public class ConversationService {
    @Autowired
    private ConversationRepository conversationRepository;

    // REGISTRA UNA NUEVA CONVERSACIÓN
    @Transactional
    public Response registerConversation(ConversationDTO request) {
        try {
            Conversation conversation = new Conversation();
            conversation.setInterestedName(request.getInterestedName());
            conversation.setPatient(request.getPatient());
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
            return new Response("Error al registrar la solicitud de contacto");
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
    public Response updateById(Long id, ConversationDTO request) {
        try {
            // VERIFICAR SI SE PROPORCIONÓ UN NUEVO ESTADO
            if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
                return new Response("Error: Se debe proporcionar un nuevo estado para la conversación"); }
            // BUSCAR LA CONVERSACIÓN POR SU ID O LANZAR UNA EXCEPCIÓN SI NO SE ENCUENTRA
            Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new Exception("Conversación no encontrada"));
            // ACTUALIZAR SOLO EL ESTADO DE LA CONVERSACIÓN SI ES DIFERENTE
            if (!Objects.equals(conversation.getStatus(), request.getStatus())) {
                conversation.setStatus(request.getStatus());
                // GUARDAR LOS CAMBIOS
                conversationRepository.save(conversation);
                return new Response("Estado de la conversación actualizado exitosamente");
            } else {
                return new Response("El estado proporcionado es igual al estado actual. No se realizaron cambios.");}
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("Error al actualizar el estado de la conversación: " + e.getMessage());
        }
    }

    private ConversationDTO convertToDTO(Conversation conversation) {
        return new ConversationDTO(
            conversation.getIdConversation(),
            conversation.getInterestedName(),
            conversation.getPatient(),
            conversation.getPhoneNumber(),
            conversation.getEmail(),
            conversation.getPatientRelationship(),
            conversation.getRequestDate(),
            conversation.getMessage(),
            conversation.getStatus()
        );
    }
}

