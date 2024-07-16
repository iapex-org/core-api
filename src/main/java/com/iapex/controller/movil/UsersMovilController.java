package com.iapex.controller.movil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import com.iapex.dto.institution.PasswordResetRequestDTO;
import com.iapex.dto.user.UserAuthenticationDTO;
import com.iapex.dto.user.UserMovilDTO;
import com.iapex.exceptions.UserAlreadyExistsException;
import com.iapex.model.response.AuthenticationResponse;
import com.iapex.model.response.Response;
import com.iapex.model.user.UserMovil;
import com.iapex.service.email.MovilEmailService;
import com.iapex.service.user.UserMovilService;

import jakarta.validation.Valid;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8100", "http://localhost:8101"})
@RequestMapping("/users")
@RestController
public class UsersMovilController {

    private final UserMovilService userMovilService;
    private final MovilEmailService movilEmailService;

    public UsersMovilController(UserMovilService userMovilService, MovilEmailService movilEmailService) {
        this.userMovilService = userMovilService;
        this.movilEmailService = movilEmailService;
    }
    
    
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    @PutMapping("/updateUser/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserMovilDTO request, BindingResult result, Authentication authentication) {
        String authenticatedEmail = authentication.getName();
        // Verificar si el usuario tiene permiso para actualizar el usuario con el ID especificado
        try {
        	UserMovil updatedUser = userMovilService.updateUserById(id, request);
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
            Optional<UserMovil> userOptional = userMovilService.getUserById(id);
            if (userOptional.isPresent()) {
            	UserMovil userMovil = userOptional.get();
                return ResponseEntity.ok(userMovil);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Error al obtener el usuario: " + e.getMessage()));
        }
    }
    

    // ENDPOINT PARA OBTENER TODOS LOS USUARIOS
    //SOLO DASHBOARD
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/getAllUsers")
    public ResponseEntity<List<UserMovil>> getAllUsers() {
        List<UserMovil> users = userMovilService.getAllUsers();
        return ResponseEntity.ok(users);
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
    

    //REGISTRO DE USUARIO EN APP MOVIL
    //http://localhost:8080/users/createUser
    @PostMapping("/createUser")
    public ResponseEntity<?> register(@Valid @RequestBody UserMovilDTO request, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        } try {
            Response response = userMovilService.register(request); 
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
   //http://localhost:8080/users/confirm?email=20223l001010@utcv.edu.mx&code=666737 
   @GetMapping("/confirm")
   public ResponseEntity<Response> confirmUser(@RequestParam("email") String email, @RequestParam("code") String code) {
       try {
    	   movilEmailService.verifyUserWithCode(email, code);
           return ResponseEntity.ok(new Response("Usuario verificado correctamente. Puedes cerrar esta pestaña."));
       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response("Error al verificar el usuario: " + e.getMessage()));
       }
   }

   
   //INICIO DE SESIÓN DE USUARIO EN MOVIL
   ///http://localhost:8080/users/login
   @PostMapping("/login")
   public ResponseEntity<?> login(@Valid @RequestBody UserAuthenticationDTO request, BindingResult result) {
       if (result.hasErrors()) {
           Map<String, String> errors = result.getFieldErrors().stream()
               .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
           return ResponseEntity.badRequest().body(errors);
       }
       try {
           AuthenticationResponse authResponse = userMovilService.authenticate(request);
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
    //http://localhost:8080/users/request-password-reset?email=20223l001010@utcv.edu.mx
   @PostMapping("/request-password-reset")
   public ResponseEntity<?> requestPasswordReset(@RequestParam String email) {
       try {
    	   UserMovil userMovil = userMovilService.findByEmail(email);
           movilEmailService.sendPasswordResetEmail(userMovil); // Esto puede lanzar excepciones propias de emailService

           return ResponseEntity.ok(new Response("Se ha enviado un correo con instrucciones para restablecer la contraseña"));
       } catch (RuntimeException e) {
           return ResponseEntity.badRequest().body(new Response(e.getMessage()));
       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Error al procesar la solicitud"));
       }
   }
   
///
    //RESTABLECER CONTRASEÑA
    //http://localhost:8080/users/reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid PasswordResetRequestDTO request) {
        try {
            boolean isVerified = userMovilService.verifyCodeAndResetPassword(
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
    //http://localhost:8080/users/resend-reset-password?email=20223l001010@utcv.edu.mx
    @PostMapping("/resend-reset-password")
    public ResponseEntity<?> resendPasswordReset(@RequestParam String email) {
        try {
        	UserMovil userMovil = userMovilService.findByEmail(email);
            if (userMovil != null) {
            	movilEmailService.sendPasswordResetEmail(userMovil);
            }
            return ResponseEntity.ok(new Response("Se ha enviado un nuevo correo con instrucciones para restablecer la contraseña"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Error al procesar la solicitud"));
        }
    }

}
