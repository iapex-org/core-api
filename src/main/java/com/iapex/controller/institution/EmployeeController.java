package com.iapex.controller.institution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import com.iapex.service.institution.UserInstitutionService;

import jakarta.validation.Valid;

import com.iapex.exceptions.InstitutionNotFoundException;
import com.iapex.exceptions.UserAlreadyExistsException;
import com.iapex.institution.DTO.UserInstitutionDTO;
import com.iapex.model.Response;
import com.iapex.model.institution.UserInstitution;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/employeeInstitution")
public class EmployeeController {
	
    @Autowired
    private UserInstitutionService userInstitutionService;
    

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationInstitutionController.class);


    // AL HACER UNA SOLICITUD GET A ESTA RUTA, SE ACCEDE A UN USUARIO ESPECÍFICO POR SU ID
    //WEB
    // http://localhost:8080/employeeInstitution/getUserInstitutionById/12
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    @GetMapping("/getUserInstitutionById/{id}")
    public ResponseEntity<?> getUserInstitutionById(@PathVariable Long id) {
        try {
            UserInstitutionDTO dto = userInstitutionService.getUserInstitutionDTOById(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response(e.getMessage()));
        }
    }
    
    
    //OBTIENE TODOS LOS USUARIOS DE LA MISMA INSTITUCIÓN QUE EL USUARIO AUTENTICADO.
    //WEB
    // http://localhost:8080/employeeInstitution/getAllSameInstitution
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    @GetMapping("/getAllSameInstitution")
    public ResponseEntity<List<UserInstitutionDTO>> getUsersFromSameInstitution(Authentication authentication) {
        String email = authentication.getName();
        List<UserInstitutionDTO> users = userInstitutionService.getUsersFromSameInstitution(email);
        return ResponseEntity.ok(users);
    }
    
    
    
    // ENDPOINT PARA ACTUALIZAR UN USUARIO
    //WEB
    // http://localhost:8080/employeeInstitution/updateUserInstitution/12
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    @PutMapping("/updateUserInstitution/{id}")
    public ResponseEntity<?> updateUserInstitution(
            @PathVariable Long id,
            @Valid @RequestBody UserInstitutionDTO request,
            BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        } 
        try {
            Response response = userInstitutionService.updateUserInstitution(id, request);
            return ResponseEntity.ok(response);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new Response(e.getMessage()));
        } catch (InstitutionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Response(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Response("Error al actualizar el usuario: " + e.getMessage()));
        }
    }
    
    
    // ENDPOINT PARA ELIMINAR UN USUARIO
    //WEB
    //http://localhost:8080/employeeInstitution/deleteUserInstitutionById/12
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    @DeleteMapping("/deleteUserInstitutionById/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable Long id) {
        try {
            userInstitutionService.deleteById(id);
            return ResponseEntity.ok(new Response("Usuario eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response("Ha ocurrido un error al intentar eliminar el usuario"));
        }
    }

}
