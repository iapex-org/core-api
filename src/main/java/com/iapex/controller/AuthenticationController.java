package com.iapex.controller;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import com.iapex.dto.UserAuthenticationDTO;
import com.iapex.dto.UserDTO;
import com.iapex.exceptions.UserAlreadyExistsException;
import com.iapex.institution.DTO.PasswordResetRequestDTO;
import com.iapex.model.AuthenticationResponse;
import com.iapex.model.Response;
import com.iapex.model.User;
import com.iapex.service.UserService;
import com.iapex.service.mail.EmailService;

import jakarta.validation.Valid;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8100", "http://localhost:8101"})
@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    private final UserService userService;
    private final EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    public AuthenticationController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }
    //REGISTRO DE USUARIO EN APP MOVIL
    //http://localhost:8080/auth/createUser
    @PostMapping("/createUser")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO request, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        } try {
            Response response = userService.register(request); 
            return ResponseEntity.ok(response);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response(e.getMessage()));
        } catch (Exception e) {
            //logger.error("Error al registrar usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Ha ocurrido un error inesperado"));
        }
    }
    
    
    /// 
   //CONFIRMAR CUENTA DE USUARIO EN MOVIL
   //http://localhost:8080/auth/confirm?email=20223l001010@utcv.edu.mx&code=666737 
   @GetMapping("/confirm")
   public ResponseEntity<Response> confirmUser(@RequestParam("email") String email, @RequestParam("code") String code) {
       try {
           emailService.verifyUserWithCode(email, code);
           return ResponseEntity.ok(new Response("Usuario verificado correctamente. Puedes cerrar esta pestaña."));
       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response("Error al verificar el usuario: " + e.getMessage()));
       }
   }

   
   //INICIO DE SESIÓN DE USUARIO EN MOVIL
   ///http://localhost:8080/auth/login
   @PostMapping("/login")
   public ResponseEntity<?> login(@Valid @RequestBody UserAuthenticationDTO request, BindingResult result) {
       if (result.hasErrors()) {
           Map<String, String> errors = result.getFieldErrors().stream()
               .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
           return ResponseEntity.badRequest().body(errors);
       }
       try {
           AuthenticationResponse authResponse = userService.authenticate(request);
           return ResponseEntity.ok(authResponse);
       } catch (RuntimeException e) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(e.getMessage()));
       } catch (Exception e) {
           //logger.error("Error durante la autenticación", e);
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Ha ocurrido un error inesperado durante la autenticación"));
       }
   }
   

///
    //SOLICITAR RESTABLECIMIENTO DE CONTRASEÑA
    //http://localhost:8080/auth/request-password-reset?email=20223l001010@utcv.edu.mx
   @PostMapping("/request-password-reset")
   public ResponseEntity<?> requestPasswordReset(@RequestParam String email) {
       try {
           User user = userService.findByEmail(email);
           emailService.sendPasswordResetEmail(user); // Esto puede lanzar excepciones propias de emailService

           return ResponseEntity.ok(new Response("Se ha enviado un correo con instrucciones para restablecer la contraseña"));
       } catch (RuntimeException e) {
           return ResponseEntity.badRequest().body(new Response(e.getMessage()));
       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Error al procesar la solicitud"));
       }
   }
   
///
    //RESTABLECER CONTRASEÑA
    //http://localhost:8080/auth/reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid PasswordResetRequestDTO request) {
        try {
            boolean isVerified = userService.verifyCodeAndResetPassword(
                request.getVerificationCode(), 
                request.getNewPassword()
            );
            if (isVerified) {
                return ResponseEntity.ok(new Response("Contraseña actualizada correctamente"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response("Código de verificación inválido o expirado"));
            }
        } catch (Exception e) {
            //logger.error("Error al restablecer la contraseña", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Error al restablecer la contraseña"));
        }
    }

    //REENVIAR CORREO DE RESTABLECIMIENTO DE CONTRASEÑA
    //http://localhost:8080/auth/resend-reset-password?email=20223l001010@utcv.edu.mx
    @PostMapping("/resend-reset-password")
    public ResponseEntity<?> resendPasswordReset(@RequestParam String email) {
        try {
            User user = userService.findByEmail(email);
            if (user != null) {
                emailService.sendPasswordResetEmail(user);
            }
            return ResponseEntity.ok(new Response("Se ha enviado un nuevo correo con instrucciones para restablecer la contraseña"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Error al procesar la solicitud"));
        }
    }

}