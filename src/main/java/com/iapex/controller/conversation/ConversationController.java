package com.iapex.controller.conversation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.iapex.dto.institution.InstitutionDTO;
import com.iapex.dto.patient.ConversationDTO;
import com.iapex.model.response.Response;
import com.iapex.model.user.UserInstitution;
import com.iapex.service.patient.ConversationService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    //CREAR CONVERSACION
    //MOVIL
    @PostMapping("/createConversation")
    public ResponseEntity<?> registerConversation(
            @Valid @RequestBody ConversationDTO request,
            BindingResult result) {
        if (result.hasErrors()) {
            // SI HAY ERRORES DE VALIDACIÓN EN EL DTO, SE RECOPILAN Y SE DEVUELVEN COMO RESPUESTA
            Map<String, String> errors = result.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }

        Response response = conversationService.registerConversation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //OBTENER CONVERSACION POR ID
    //WEB
    //http://localhost:8080/conversations/getConversationById/3
    @GetMapping("/getConversationById/{id}")
    public ResponseEntity<?> getConversationById(@PathVariable Long id) {
        try {
            ConversationDTO conversation = conversationService.getConversationById(id);
            return new ResponseEntity<>(conversation, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new Response(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    //OBTENER CONVERSACIONES
    //WEB
    // http://localhost:8080/conversations/getAllConversations
    @GetMapping("/getAllConversations")
    public ResponseEntity<List<ConversationDTO>> getAllConversations() {
        List<ConversationDTO> conversations = conversationService.getAllConversations();
        return new ResponseEntity<>(conversations, HttpStatus.OK);
    }
    
    
    // OBTENER LAS CONVERSACIONES DE LA MISMA INSTITUCIÓN QUE EL USUARIO AUTENTICADO, IDEAL PARA USARLOS EN EL DASHBOARD, CUANDO UN EMPLEADO INGRESE SOLO SE LE MOSTRARA CONVERSACIONES DE SU INSTITUCION
    //WEB
    //http://localhost:8080/conversations/getConversationsByInstitution
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/getConversationsByInstitution")
    public ResponseEntity<List<ConversationDTO>> getConversationsForAuthenticatedUser() {
        try {
            // OBTENER LA INFORMACIÓN DEL USUARIO AUTENTICADO
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            @SuppressWarnings("unused")
			UserInstitution userInstitution = (UserInstitution) authentication.getPrincipal();
            // LLAMAR AL SERVICIO PARA OBTENER LAS CONVERSACIONES DE LA MISMA INSTITUCIÓN
            List<ConversationDTO> conversations = conversationService.getConversationsForAuthenticatedUser();
            return new ResponseEntity<>(conversations, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //ACTUALIZAR LA CONVERSACION, SOLO SE PUEDE EL STATUS: Y SE CAMBIA EN EL JSON
    //http://localhost:8080/conversations/updateConversationById/5
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/updateConversationById/{id}")
    public ResponseEntity<Response> updateConversationStatus(@PathVariable Long id, @RequestBody ConversationDTO request) {
        // OBTENER LA INFORMACIÓN DEL USUARIO AUTENTICADO
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInstitution userInstitution = (UserInstitution) authentication.getPrincipal();
        // LLAMAR AL SERVICIO CON LA INFORMACIÓN DEL USUARIO
        Response response = conversationService.updateById(id, request, 
            userInstitution.getName(), 
            userInstitution.getFathername(), 
            userInstitution.getMothername());
    
        return new ResponseEntity<>(response, HttpStatus.OK);
    }  
}

