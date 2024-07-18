package com.iapex.controllers;

import com.iapex.dtos.contactRequest.ContactRequestDTO;
import com.iapex.dtos.contactRequest.UpdateContactRequestDTO;
import com.iapex.models.response.Response;
import com.iapex.services.ContactRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contact-requests")
public class ContactRequestController {

    @Autowired
    private ContactRequestService contactRequestService;

    // Obtiene todas las solicitudes de contacto
    @GetMapping
    public ResponseEntity<List<ContactRequestDTO>> getAllContactRequests() {
        List<ContactRequestDTO> contactRequests = contactRequestService.getAllContactRequests();
        return new ResponseEntity<>(contactRequests, HttpStatus.OK);
    }

    // Obtiene una solicitud de contacto por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getContactRequestById(@PathVariable Long id) {
        try {
            ContactRequestDTO contactRequest = contactRequestService.getContactRequestById(id);
            return new ResponseEntity<>(contactRequest, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new Response(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    // Obtiene las solicitudes de contacto por la institución del usuario
    // autenticado
    @GetMapping("/current-user/institution")
    public ResponseEntity<List<ContactRequestDTO>> getContactRequestsByInstitution() {
        List<ContactRequestDTO> contactRequests = contactRequestService.getContactRequestsByInstitution();
        return new ResponseEntity<>(contactRequests, HttpStatus.OK);
    }

    // Crea una solicitud de contacto
    @PostMapping
    public ResponseEntity<?> createContactRequest(@Valid @RequestBody ContactRequestDTO request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }

        try {
            Response response = contactRequestService.createContactRequest(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new Response("Error al registrar la solicitud de contacto: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualiza una solicitud de contacto por ID
    @PutMapping("/{id}")
    public ResponseEntity<Response> updateContactRequestById(@PathVariable Long id,
            @RequestBody UpdateContactRequestDTO request) {
        Response response = contactRequestService.updateContactRequestById(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
