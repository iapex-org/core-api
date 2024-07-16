package com.iapex.controller.institution;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import com.iapex.dto.user.PasswordResetRequestDTO;
import com.iapex.dto.user.UserInstitutionAuthenticationDTO;
import com.iapex.dto.user.UserInstitutionDTO;
import com.iapex.exceptions.InstitutionNotFoundException;
import com.iapex.exceptions.UserAlreadyExistsException;
import com.iapex.model.response.AuthenticationResponse;
import com.iapex.model.response.Response;
import com.iapex.model.user.UserInstitution;
import com.iapex.service.email.InstitutionEmailService;
import com.iapex.service.user.UserInstitutionService;

import jakarta.validation.Valid;


import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/userInstitution")
public class AuthInstitutionController {

    private final UserInstitutionService userInstitutionService;
    private final InstitutionEmailService institutionEmailService;

    public AuthInstitutionController(UserInstitutionService userInstitutionService, InstitutionEmailService institutionEmailService) {
        this.userInstitutionService = userInstitutionService;
        this.institutionEmailService = institutionEmailService;
    }

    //CREAR UN USUARIO PARA REGISTRO
    //WEB
    //http://localhost:8080/userInstitution/createUserInstitution
    @PostMapping("/createUserInstitution")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserInstitutionDTO request, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Response response = userInstitutionService.registerUser(request);
            return ResponseEntity.ok(response);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response(e.getMessage()));
        } catch (InstitutionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(e.getMessage()));
        } catch (Exception e) {
            //logger.error("Error registering user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Ha ocurrido un error inesperado"));
        }
    }
    

    //EL USUARIO CONFIRMA QUE LA CUENTA ES REAL
    //WEB
    //http://localhost:8080/userInstitution/confirm?email=misraelaltamirano@gmail.com&code=350231
    @GetMapping("/confirm")
    public ResponseEntity<?> confirmUserInstitution(@RequestParam("email") String email, @RequestParam("code") String code) {
        try {
            institutionEmailService.verifyUserInstitutionWithCode(email, code);
            return ResponseEntity.ok(new Response("Usuario verificado correctamente"));
        } catch (Exception e) {
            //logger.error("Error verifying user", e);
            return ResponseEntity.badRequest().body(new Response("Error al verificar el usuario: " + e.getMessage()));
        }
    }

    
    //EL USUARIO SOLICITA REENVIAR EL CODIGO DE CONFIRMACION
    //WEB
    //http://localhost:8080/userInstitution/resend-verification-confirm?email=misraelaltamirano@gmail.com
    @GetMapping("/resend-verification-confirm")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
        try {
            institutionEmailService.resendVerificationCode(email);
            return ResponseEntity.ok(new Response("Nuevo código de verificación enviado"));
        } catch (Exception e) {
            //logger.error("Error resending verification code", e);
            return ResponseEntity.badRequest().body(new Response(e.getMessage()));
        }
    }
    

    //EL USUARIO ENTRA A LA APLICACION Y SE LOGUEA
    //WEB
    //http://localhost:8080/userInstitution/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserInstitutionAuthenticationDTO request, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            AuthenticationResponse authResponse = userInstitutionService.authenticateInstitution(request);
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(e.getMessage()));
        } catch (Exception e) {
            //logger.error("Error during authentication", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Ha ocurrido un error inesperado durante la autenticación"));
        }
    }

    
    //EL USUARIO SOLICITA EL CORREO CON EL CODIGO PARA RESTABLECER LA CONTRASEÑA
    //WEB
    //http://localhost:8080/userInstitution/request-password-reset?email=misraelaltamirano@gmail.com
    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email) {
        try {
            UserInstitution userInstitution = userInstitutionService.findByEmail(email);
            if (userInstitution != null) {
                institutionEmailService.sendPasswordResetUserInstitutionEmail(userInstitution);
                return ResponseEntity.ok(new Response("Se ha enviado un correo con instrucciones para restablecer la contraseña"));
            }
        } catch (Exception e) {
            //logger.error("Error processing password reset request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Error al procesar la solicitud: " + e.getMessage()));
        }
        return ResponseEntity.badRequest().body(new Response("El correo no está registrado en la aplicación."));
    }
    

    //EL USUARIO RESTABLECE SU CONTRASEÑA
    //WEB
    //http://localhost:8080/userInstitution/reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid PasswordResetRequestDTO request) {
        try {
            boolean isVerified = userInstitutionService.verifyCodeAndResetPassword(
                request.getVerificationCode(), 
                request.getNewPassword()
            );
            if (isVerified) {
                return ResponseEntity.ok(new Response("Contraseña actualizada correctamente"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response("Código de verificación inválido o expirado"));
            }
        } catch (Exception e) {
            //logger.error("Error resetting password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Error al restablecer la contraseña"));
        }
    }

    
    //ENVIAR DE NUEVO EL CORREO CON EL CODIGO PARA RESTABLECER LA CONTRASEÑA
    //WEB
    //http://localhost:8080/userInstitution/resend-reset-password?email=misraelaltamirano@gmail.com
    @PostMapping("/resend-reset-password")
    public ResponseEntity<?> resendPasswordReset(@RequestParam String email) {
        try {
            UserInstitution userInstitution = userInstitutionService.findByEmail(email);
            if (userInstitution != null) {
                institutionEmailService.sendPasswordResetUserInstitutionEmail(userInstitution);
                return ResponseEntity.ok(new Response("Se ha enviado un nuevo correo con instrucciones para restablecer la contraseña."));
            }
        } catch (Exception e) {
            //logger.error("Error resending password reset email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Error al procesar la solicitud: " + e.getMessage()));
        }
        return ResponseEntity.badRequest().body(new Response("El correo no está registrado en la aplicación."));
    }

}