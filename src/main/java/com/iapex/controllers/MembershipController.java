package com.iapex.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.iapex.dtos.MembershipDTO;
import com.iapex.exceptions.InstitutionAlreadyExistsException;
import com.iapex.exceptions.InstitutionNotFoundException;
import com.iapex.models.response.Response;
import com.iapex.services.MembershipService;

@RestController
@RequestMapping("/api/v1/memberships")
public class MembershipController {

    @Autowired
    private MembershipService membershipService;

    // Obtener todas las membresías
    // @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<List<MembershipDTO>> getAllMemberships() {
        List<MembershipDTO> memberships = membershipService.getAllMemberships();
        return ResponseEntity.ok(memberships);
    }

    // Obtener membresía por ID
    // @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getMembershipById(@PathVariable Long id) {
        try {
            MembershipDTO membershipDTO = membershipService.getMembershipById(id);
            return ResponseEntity.ok(membershipDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(e.getMessage()));
        }
    }

    // Formato que admite 2024-07-15T01:56:00
    // Crear una membresía
    // @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<Response> createMembership(@RequestBody MembershipDTO request) {
        try {
            Response response = membershipService.registerMembership(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (InstitutionNotFoundException e) {
            return new ResponseEntity<>(new Response(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (InstitutionAlreadyExistsException e) {
            return new ResponseEntity<>(new Response(e.getMessage()), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>(new Response("Error inesperado al registrar la membresía: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> updateMembership(@PathVariable Long id, @RequestBody MembershipDTO request) {
        try {
            Response response = membershipService.updateById(id, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (InstitutionNotFoundException e) {
            return new ResponseEntity<>(new Response(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            // Manejo de la excepción cuando la membresía está desactivada
            return new ResponseEntity<>(new Response(e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(new Response("Error inesperado al actualizar la membresía: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteMembership(@PathVariable Long id) {
        try {
            Response response = membershipService.deleteMembership(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response("Error al desactivar la membresía: " + e.getMessage()));
        }
    }
}