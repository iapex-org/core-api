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
import com.iapex.exceptions.AuthenticateEmailException;
import com.iapex.models.response.AuthenticationResponse;
import com.iapex.models.response.Response;
import com.iapex.models.user.UserWeb;
import com.iapex.services.email.WebEmailService;
import com.iapex.services.user.UserWebService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users/web")
public class UserWebController {

    private final UserWebService userWebService;
    private final WebEmailService webEmailService;

    public UserWebController(UserWebService userWebService, WebEmailService webEmailService) {
        this.userWebService = userWebService;
        this.webEmailService = webEmailService;
    }

    // Obtener todos los usuarios
    // @PreAuthorize("hasAuthority('SUPER_ADMIN')")
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
    @GetMapping("/me/institution")
    public ResponseEntity<List<UserWebDTO>> getUsersByInstitution(Authentication authentication) {
        String email = authentication.getName();
        List<UserWebDTO> users = userWebService.getUsersFromSameInstitution(email);
        return ResponseEntity.ok(users);
    }

    // Confirmar usuario
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

    // Solicitar correo con el código de verificación
    @GetMapping("/confirm/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
        try {
            System.out.println("Iniciando reenvío del código de verificación para: " + email);

            // Intentar reenviar el código de verificación de manera sincrónica
            try {
                String verificationCode = webEmailService.resendVerificationCode(email).join(); // Bloquear hasta que
                                                                                                // termine
                System.out.println("Código reenviado exitosamente: " + verificationCode);
                return ResponseEntity.ok()
                        .body(new Response("El código de verificación ha sido reenviado exitosamente."));
            } catch (Exception e) {
                System.err.println("Error durante el reenvío del código de verificación: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new Response(
                                "Ocurrió un error al intentar reenviar el código de verificación. Intente nuevamente más tarde."));
            }
        } catch (Exception e) {
            System.err.println("Error interno en el servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response("Error al procesar la solicitud: " + e.getMessage()));
        }
    }

    // Obtener datos del usuario autenticado
    @GetMapping("/me")
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
            // Manejo de excepción personalizada con código 442
        } catch (AuthenticateEmailException e) {
            return ResponseEntity.status(442).body(new Response(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response("Ha ocurrido un error inesperado durante la autenticación"));
        }
    }

    // Solicitar correo con el código para restablecer la contraseña
    @PostMapping("/password-reset/request")
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email) {
        try {
            System.out.println("Iniciando solicitud de restablecimiento de contraseña para: " + email);

            // Buscar el usuario por correo electrónico
            UserWeb userWeb = userWebService.findByEmail(email);
            if (userWeb != null) {
                System.out.println("Usuario encontrado: " + userWeb.getUsername());

                // Enviar correo sin asincronismo para garantizar que el cliente reciba feedback
                // sobre el resultado
                try {
                    webEmailService.sendPasswordResetEmailAsync(userWeb).join(); // Bloquear hasta que termine
                    return ResponseEntity.ok()
                            .body(new Response("Correo enviado exitosamente. Revise su bandeja de entrada."));
                } catch (Exception e) {
                    System.err.println("Error durante el envío de correo: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new Response(
                                    "Ocurrió un error al enviar el correo. Intente nuevamente o informe al administrador."));
                }
            }

            System.out.println("Correo no encontrado: " + email);
            return ResponseEntity.badRequest()
                    .body(new Response("El correo no está registrado en la aplicación."));
        } catch (Exception e) {
            System.err.println("Error interno en el servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response("Error al procesar la solicitud: " + e.getMessage()));
        }
    }

    // Reenviar el correo con el código para restablecer la contraseña
    @PostMapping("/password-reset/resend")
    public ResponseEntity<?> resendPasswordReset(@RequestParam String email) {
        try {
            System.out.println("Iniciando reenvío de correo para restablecimiento de contraseña para: " + email);

            // Buscar el usuario por correo electrónico
            UserWeb userWeb = userWebService.findByEmail(email);
            if (userWeb != null) {
                System.out.println("Usuario encontrado: " + userWeb.getUsername());

                try {
                    // Enviar el correo de manera sincrónica
                    String verificationCode = webEmailService.sendPasswordResetEmailAsync(userWeb).join();
                    System.out.println("Correo reenviado exitosamente. Código de verificación: " + verificationCode);

                    return ResponseEntity.ok()
                            .body(new Response(
                                    "Se ha enviado exitosamente un nuevo correo con instrucciones para restablecer la contraseña. Revise su bandeja de entrada."));
                } catch (Exception e) {
                    System.err.println("Error durante el reenvío del correo de restablecimiento: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new Response(
                                    "Ocurrió un error al intentar reenviar el correo de restablecimiento. Intente nuevamente más tarde."));
                }
            }

            System.out.println("Correo no registrado: " + email);
            return ResponseEntity.badRequest()
                    .body(new Response("El correo no está registrado en la aplicación."));
        } catch (Exception e) {
            System.err.println("Error interno en el servidor: " + e.getMessage());
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
}