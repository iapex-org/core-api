package com.iapex.controller.membership;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iapex.dto.institution.MembershipDTO;
import com.iapex.exceptions.InstitutionAlreadyExistsException;
import com.iapex.exceptions.InstitutionNotFoundException;
import com.iapex.model.response.Response;
import com.iapex.service.membership.MembershipService;

@RestController
@RequestMapping("/memberships")
public class MembershipController {

    @Autowired
    private MembershipService membershipService;

    //FORMATO QUE ADMITE 2024-07-15T01:56:00
    //REGUSTRAR UNA MEMBRESIA
    @PostMapping("/registerMembership")
    public ResponseEntity<Response> registerMembership(@RequestBody MembershipDTO request) {
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
    
    @PutMapping("/updateMembershipById/{id}")
    public ResponseEntity<Response> updateMembership(@PathVariable Long id, @RequestBody MembershipDTO request) {
        try {
            Response response = membershipService.updateById(id, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (InstitutionNotFoundException e) {
            return new ResponseEntity<>(new Response(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new Response("Error inesperado al actualizar la membresía: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    //OBTENER UNA MEMBRECIA POR ID
    @GetMapping("/getMembershipById/{id}")
    public ResponseEntity<?> getMembershipById(@PathVariable Long id) {  try {
            MembershipDTO membershipDTO = membershipService.getMembershipById(id);
            return ResponseEntity.ok(membershipDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(e.getMessage()));
        }
    }
    
    //OBTENER TODAS LAS MEMBRECIAS
    @GetMapping("/getAllMemberships")
    public ResponseEntity<List<MembershipDTO>> getAllMemberships() {
        List<MembershipDTO> memberships = membershipService.getAllMemberships();
        return ResponseEntity.ok(memberships);
    }
}