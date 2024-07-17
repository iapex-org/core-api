package com.iapex.controllers;

import com.iapex.dtos.ContactRequestDTO;
import com.iapex.models.response.Response;
import com.iapex.models.user.UserWeb;
import com.iapex.services.ContactRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/contact-requests")
public class ContactRequestController {

    @Autowired
    private ContactRequestService contactRequestService;

    // Obtener todas las solicitudes de contacto
    @GetMapping
    public ResponseEntity<List<ContactRequestDTO>> getAllContactRequests() {
        List<ContactRequestDTO> contactRequests = contactRequestService.getAllContactRequests();
        return new ResponseEntity<>(contactRequests, HttpStatus.OK);
    }

    // Obtener solicitud de contacto por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getContactRequestById(@PathVariable Long id) {
        try {
            ContactRequestDTO contactRequest = contactRequestService.getContactRequestById(id);
            return new ResponseEntity<>(contactRequest, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new Response(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    // Obtener las solicitudes de contacto de la misma institución que el usuario
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/current-user/institution")
    public ResponseEntity<List<ContactRequestDTO>> getContactRequestsByInstitution() {
        try {
            // Obtener las solicitudes de contacto de la misma institución
            List<ContactRequestDTO> contactRequests = contactRequestService.getContactRequestsByInstitution();
            return new ResponseEntity<>(contactRequests, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Crear solicitud de contacto
    @PostMapping
    public ResponseEntity<?> createContactRequest(
            @Valid @RequestBody ContactRequestDTO request,
            BindingResult result) {
        if (result.hasErrors()) {
            // Devolver errores de validación del DTO
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }

        Response response = contactRequestService.createContactRequest(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Actualizar solicitud de contacto por ID
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Response> updateContactRequest(@PathVariable Long id,
            @RequestBody ContactRequestDTO request) {
        // Obtener información del usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserWeb userWeb = (UserWeb) authentication.getPrincipal();
        // Llamar al servicio para actualizar la solicitud de contacto
        Response response = contactRequestService.updateContactRequestById(id, request, userWeb.getName(),
                userWeb.getLastName(), userWeb.getSecondLastName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
