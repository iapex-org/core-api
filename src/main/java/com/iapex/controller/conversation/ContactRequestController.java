package com.iapex.controller.conversation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.iapex.dto.institution.ContactRequestDTO;
import com.iapex.model.institution.UserInstitution;
import com.iapex.model.response.Response;
import com.iapex.service.patient.ContactRequestService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@RestController
@RequestMapping("/contact-requests")
public class ContactRequestController  {

    @Autowired
    private ContactRequestService contactRequestService;

    //http://localhost:8080/contact-requests/getConversationById/3
    //CREAR CONVERSACION
    //MOVIL
    @PostMapping("/createConversation")
    public ResponseEntity<?> registerConversation(
            @Valid @RequestBody ContactRequestDTO request,
            BindingResult result) {
        if (result.hasErrors()) {
            // SI HAY ERRORES DE VALIDACIÓN EN EL DTO, SE RECOPILAN Y SE DEVUELVEN COMO RESPUESTA
            Map<String, String> errors = result.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }

        Response response = contactRequestService.registerConversation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //OBTENER CONVERSACION POR ID
    //WEB
    //http://localhost:8080/contact-requests/getConversationById/3
    @GetMapping("/getConversationById/{id}")
    public ResponseEntity<?> getConversationById(@PathVariable Long id) {
        try {
        	ContactRequestDTO conversation = contactRequestService.getConversationById(id);
            return new ResponseEntity<>(conversation, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new Response(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    //OBTENER CONVERSACIONES
    //WEB
    // http://localhost:8080/contact-requests/getAllConversations
    @GetMapping("/getAllConversations")
    public ResponseEntity<List<ContactRequestDTO>> getAllConversations() {
        List<ContactRequestDTO> conversations = contactRequestService.getAllConversations();
        return new ResponseEntity<>(conversations, HttpStatus.OK);
    }
    
    
    // OBTENER LAS CONVERSACIONES DE LA MISMA INSTITUCIÓN QUE EL USUARIO AUTENTICADO, IDEAL PARA USARLOS EN EL DASHBOARD, CUANDO UN EMPLEADO INGRESE SOLO SE LE MOSTRARA CONVERSACIONES DE SU INSTITUCION
    //WEB
    //http://localhost:8080/contact-requests/getConversationsByInstitution
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/getConversationsByInstitution")
    public ResponseEntity<List<ContactRequestDTO>> getConversationsForAuthenticatedUser() {
        try {
            // OBTENER LA INFORMACIÓN DEL USUARIO AUTENTICADO
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            @SuppressWarnings("unused")
			UserInstitution userInstitution = (UserInstitution) authentication.getPrincipal();
            // LLAMAR AL SERVICIO PARA OBTENER LAS CONVERSACIONES DE LA MISMA INSTITUCIÓN
            List<ContactRequestDTO> conversations = contactRequestService.getConversationsForAuthenticatedUser();
            return new ResponseEntity<>(conversations, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //ACTUALIZAR LA CONVERSACION, SOLO SE PUEDE EL STATUS: Y SE CAMBIA EN EL JSON
    //http://localhost:8080/contact-requests/updateConversationById/5
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/updateConversationById/{id}")
    public ResponseEntity<Response> updateConversationStatus(@PathVariable Long id, @RequestBody ContactRequestDTO request) {
        // OBTENER LA INFORMACIÓN DEL USUARIO AUTENTICADO
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInstitution userInstitution = (UserInstitution) authentication.getPrincipal();
        // LLAMAR AL SERVICIO CON LA INFORMACIÓN DEL USUARIO
        Response response = contactRequestService.updateById(id, request, 
            userInstitution.getName(), 
            userInstitution.getFathername(), 
            userInstitution.getMothername());
    
        return new ResponseEntity<>(response, HttpStatus.OK);
    }  
}

