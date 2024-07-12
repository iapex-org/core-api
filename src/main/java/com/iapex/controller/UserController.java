package com.iapex.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.iapex.dto.UserDTO;
import com.iapex.model.Response;
import com.iapex.model.User;
import com.iapex.service.UserService;
import com.iapex.service.mail.EmailService;

import jakarta.validation.Valid;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8100", "http://localhost:8101"})
@RequestMapping("/users")
@RestController
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    public UserController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }
    
    
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    @PutMapping("/updateUser/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO request, BindingResult result, Authentication authentication) {
        String authenticatedEmail = authentication.getName();
        // Verificar si el usuario tiene permiso para actualizar el usuario con el ID especificado
        try {
            User updatedUser = userService.updateUserById(id, request);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Error al actualizar el usuario: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    @GetMapping("/getUserById/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, Authentication authentication) {
        // Verificar si el usuario tiene permiso para acceder al usuario con el ID especificado
        try {
            Optional<User> userOptional = userService.getUserById(id);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Error al obtener el usuario: " + e.getMessage()));
        }
    }

	 // ENDPOINT PARA ENVIAR UN CORREO A TRAVÉS DE LA APLICACIÓN
	 // LOS MENSAJES LLEGAN AL CORREO DE IAPEX6500@GMAIL.COM
	 // DARLE UNA FUNCIONALIDAD SI ES NECESARIO EN LA APP: POR EJEMPLO, UN APARTADO DE CONTÁCTANOS
	 //@PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
	 //@PostMapping("/sendEmail")
	 //public ResponseEntity<?> sendEmail(@Valid @RequestBody EmailDTO request, BindingResult result) {
	//     if (result.hasErrors()) {
	//         Map<String, String> errors = result.getFieldErrors().stream()
	//             .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
	//         return ResponseEntity.badRequest().body(errors);
	//     }
	 //
	//     try {
	//         emailService.sendEmail(request.getEmail(), request.getBody());
	//         return ResponseEntity.ok(new Response("Correo electrónico enviado exitosamente"));
	//     } catch (Exception e) {
	//         return ResponseEntity.badRequest().body(new Response("Error al enviar el correo electrónico: " + e.getMessage()));
	//     }
	 //}

}
