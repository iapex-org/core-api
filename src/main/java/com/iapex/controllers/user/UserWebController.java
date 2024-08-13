package com.iapex.controllers.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import com.iapex.dtos.user.PasswordResetRequestDTO;
import com.iapex.dtos.user.UserWebAuthenticationDTO;
import com.iapex.dtos.user.UserWebDTO;
import com.iapex.exceptions.InstitutionNotFoundException;
import com.iapex.exceptions.UserAlreadyExistsException;
import com.iapex.models.response.AuthenticationResponse;
import com.iapex.models.response.Response;
import com.iapex.models.user.UserWeb;
import com.iapex.services.email.WebEmailService;
import com.iapex.services.user.UserWebService;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/userWeb")
public class UserWebController {

    private final UserWebService userWebService;
    private final WebEmailService webEmailService;

    public UserWebController(UserWebService userWebService, WebEmailService webEmailService) {
        this.userWebService = userWebService;
        this.webEmailService = webEmailService;
    }

    // Obttener todos los usuarios
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserWebDTO>> getAllUsers() {
        List<UserWebDTO> userDTOs = userWebService.getAllUserDTOs();
        return ResponseEntity.ok(userDTOs);
    }

    // Obtener usuario por ID
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserWebById(@PathVariable Long id) {
        try {
            UserWebDTO dto = userWebService.getUserWebDTOById(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response(e.getMessage()));
        }
    }

    // Obtener todos los usuarios asociados a la institución del usuario autenticado
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping("/current/institution")
    public ResponseEntity<List<UserWebDTO>> getUsersByInstitution(Authentication authentication) {
        String email = authentication.getName();
        List<UserWebDTO> users = userWebService.getUsersFromSameInstitution(email);
        return ResponseEntity.ok(users);
    }

    // Crear un usuario para registro
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserWebDTO request, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Response response = userWebService.registerUser(request);
            return ResponseEntity.ok(response);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response(e.getMessage()));
        } catch (InstitutionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response("Ha ocurrido un error inesperado"));
        }
    }

    // Actualizar un usuario
    @PreAuthorize("hasAuthority('USER_WEB')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserWeb(
            @PathVariable Long id,
            @Valid @RequestBody UserWebDTO request,
            BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            Response response = userWebService.updateUserWeb(id, request);
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

    // Eliminar un usuario
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable Long id) {
        try {
            userWebService.deleteById(id);
            return ResponseEntity.ok(new Response("Usuario eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response("Ha ocurrido un error al intentar eliminar el usuario"));
        }
    }

    // Login de usuario
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserWebAuthenticationDTO request, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            AuthenticationResponse authResponse = userWebService.authenticateWeb(request);
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response("Ha ocurrido un error inesperado durante la autenticación"));
        }
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirmUser(@RequestParam("code") String code) {
        try {
            webEmailService.verifyUserWebWithCode(code);
            return ResponseEntity.ok(new Response("Usuario verificado correctamente"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response("Usuario no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response("Error al verificar el usuario: " + e.getMessage()));
        }
    }
    
    @GetMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
        try {
            CompletableFuture<String> future = webEmailService.resendVerificationCode(email);
            ResponseEntity<Response> response = ResponseEntity.accepted()
                .body(new Response("Se ha iniciado el proceso de reenvío del código de verificación"));
            future.thenAccept(verificationCode -> {
            }).exceptionally(ex -> { return null;});
            return response;
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new Response("Error al iniciar el proceso de reenvío: " + e.getMessage()));
        }
    }


 // Solicitar correo con el código para restablecer la contraseña
    @PostMapping("/password-reset/request")
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email) {
        try {
            UserWeb userWeb = userWebService.findByEmail(email);
            if (userWeb != null) {
                CompletableFuture<String> future = webEmailService.sendPasswordResetUserWebEmailAsync(userWeb);
                future.exceptionally(ex -> {
                    return null;
                });
                return ResponseEntity.accepted()
                    .body(new Response("Se ha iniciado el proceso de envío de instrucciones para restablecer la contraseña"));
            }
            return ResponseEntity.badRequest().body(new Response("El correo no está registrado en la aplicación."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response("Error al procesar la solicitud: " + e.getMessage()));
        }
    }

    // Reenviar el correo con el código para restablecer la contraseña
    @PostMapping("/password-reset/resend")
    public ResponseEntity<?> resendPasswordReset(@RequestParam String email) {
        try {
            UserWeb userWeb = userWebService.findByEmail(email);
            if (userWeb != null) {
                CompletableFuture<String> future = webEmailService.sendPasswordResetUserWebEmailAsync(userWeb);
                 future.exceptionally(ex -> {
                    return null;
                });
                return ResponseEntity.accepted()
                    .body(new Response("Se ha iniciado el proceso de envío de un nuevo correo con instrucciones para restablecer la contraseña."));
            }
            return ResponseEntity.badRequest().body(new Response("El correo no está registrado en la aplicación."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response("Error al procesar la solicitud: " + e.getMessage()));
        }
    }
    
    // Restablecer la contraseña
    @PostMapping("/password-reset")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid PasswordResetRequestDTO request) {
        try {
            boolean isVerified = userWebService.verifyCodeAndResetPassword(
                    request.getVerificationCode(),
                    request.getNewPassword());
            if (isVerified) {
                return ResponseEntity.ok(new Response("Contraseña actualizada correctamente"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response("Código de verificación inválido o expirado"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response("Error al restablecer la contraseña"));
        }
    }
    
    //Obtener datos del usuario autenticado
    @GetMapping("/myAccount")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            UserWeb userWeb = (UserWeb) authentication.getPrincipal();
            UserWebDTO userWebDTO = userWebService.convertToDTO(userWeb);
            return ResponseEntity.ok(userWebDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response("Error al obtener los datos del usuario: " + e.getMessage()));
        }
    }
}