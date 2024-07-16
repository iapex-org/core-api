package com.iapex.controller.institution;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.iapex.dto.institution.InstitutionDTO;
import com.iapex.dto.user.UserInstitutionDTO;
import com.iapex.exceptions.InstitutionAlreadyExistsException;
import com.iapex.exceptions.InstitutionNotFoundException;
import com.iapex.exceptions.UserAlreadyExistsException;
import com.iapex.model.institution.Institution;
import com.iapex.model.response.Response;
import com.iapex.service.files.StorageService;
import com.iapex.service.institution.InstitutionService;
import com.iapex.service.user.UserInstitutionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/institutions")
public class InstitutionController {

    @Autowired
    private InstitutionService institutionService;
    
    @Autowired
    private StorageService storageService;

    @Autowired
    private HttpServletRequest request;
    
    @Autowired
    private UserInstitutionService userInstitutionService;
    
    
    // AL HACER UNA SOLICITUD POST A ESTA RUTA, SE CREA UNA NUEVA INSTITUCIÓN CON DATOS MULTIPART
    //EL BODY SE ENVIA A TRAVEZ DE UN FORMDATA
    //ADMIN-WEB
    //http://localhost:8080/institutions/createInstitution
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(value = "/createInstitution", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(
            @Valid @ModelAttribute InstitutionDTO request,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
     try {
         if (imageFile != null && !imageFile.isEmpty()) {
             // SI SE PROPORCIONÓ UNA IMAGEN
             String originalFilename = imageFile.getOriginalFilename();
             // SE GENERA UN NOMBRE DE ARCHIVO ÚNICO PARA EVITAR CONFLICTOS
             String uniqueFilename = storageService.generateUniqueFilename(originalFilename);
             // SE ALMACENA EL ARCHIVO Y SE OBTIENE EL NOMBRE CON EL QUE SE GUARDÓ
             String storedFilename = storageService.saveFile(imageFile, uniqueFilename);

             // SE CONSTRUYE LA URL COMPLETA PARA ACCEDER A LA IMAGEN
             String host = this.request.getRequestURL().toString().replace(this.request.getRequestURI(), "");
             String imageUrl = ServletUriComponentsBuilder
                 .fromHttpUrl(host)
                 .path("/institutions/")
                 .path(storedFilename)
                 .toUriString();
             // SE ESTABLECEN EL NOMBRE DEL ARCHIVO Y LA URL EN EL DTO
             request.setImage(storedFilename);
             request.setImageUrl(imageUrl);
         }
         // SE LLAMA AL SERVICIO PARA REGISTRAR LA INSTITUCIÓN
         Response response = institutionService.register(request);
         return ResponseEntity.ok(response);
     } catch (InstitutionAlreadyExistsException e) {
         // SI LA INSTITUCIÓN YA EXISTE, SE DEVUELVE UN ERROR 400
         return ResponseEntity.badRequest().body(new Response(e.getMessage()));
     } catch (Exception e) {
         // PARA CUALQUIER OTRO ERROR, SE REGISTRA Y SE DEVUELVE UN ERROR 500
         e.printStackTrace();
         return ResponseEntity.internalServerError().body(new Response("Ha ocurrido un error: " + e.getMessage()));
     }
    }
    
    
    // AL HACER UNA SOLICITUD PUT A ESTA RUTA, SE ACTUALIZA UNA INSTITUCIÓN ESPECÍFICA POR SU ID
    // SOLO EL USUARIO CON LAS CREDENCIALES NESESARIAS PODRA ACCEDER AL RECURSO
    //EL BODY SE ENVIA A TRAVEZ DE UN FORMDATA
    //ADMIN-WEB
    //http://localhost:8080/institutions/updateInstitution/12
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('ADMIN')")
    @PutMapping("/updateInstitution/{id}")
    public ResponseEntity<?> updateInstitution(
            @PathVariable Long id,
            @Valid @ModelAttribute InstitutionDTO request,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
            ) {
        
        if (result.hasErrors()) {
            // SI HAY ERRORES DE VALIDACIÓN EN EL DTO, SE RECOPILAN Y SE DEVUELVEN COMO RESPUESTA
            Map<String, String> errors = result.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            // OBTENER LA INSTITUCIÓN ACTUAL PARA VERIFICAR Y ELIMINAR LA IMAGEN ANTERIOR SI ES NECESARIO
            Institution institution = institutionService.getInstitutionById(id);
            String oldImageFilename = institution.getImage();
            // SI HAY UN ARCHIVO DE IMAGEN NUEVO, PROCESARLO Y ACTUALIZAR LA URL EN EL DTO
            if (imageFile != null && !imageFile.isEmpty()) {
                String originalFilename = imageFile.getOriginalFilename();
                String uniqueFilename = storageService.generateUniqueFilename(originalFilename);
                String storedFilename = storageService.saveFile(imageFile, uniqueFilename);

                String host = this.request.getRequestURL().toString().replace(this.request.getRequestURI(), "");
                String imageUrl = ServletUriComponentsBuilder
                        .fromHttpUrl(host)
                        .path("/institutions/")
                        .path(storedFilename)
                        .toUriString();

                request.setImage(storedFilename);
                request.setImageUrl(imageUrl);

                // ELIMINAR EL ARCHIVO DE IMAGEN ANTERIOR SI EXISTÍA
                if (oldImageFilename != null) {
                    storageService.deleteFile(oldImageFilename);
                }
            }
            // LLAMAR AL SERVICIO PARA ACTUALIZAR LA INSTITUCIÓN
            Response response = institutionService.updateInstitution(id, request);
            return ResponseEntity.ok(response);
        } catch (InstitutionAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(new Response(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new Response("Ha ocurrido un error: " + e.getMessage()));
        }
    }

  
    
    //ELIMINAR POR COMPLETO UNA INSTITUCION DENTRO DEL DASHBOARD
    //http://localhost:8080/institutions/deleteInstitution/15
    //ADMIN-WEB
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/deleteInstitution/{id}")
    public ResponseEntity<?> deleteInstitution(@PathVariable Long id) {
        try {
        	// OBTENER LA INSTITUCIÓN ANTES DE ELIMINARLA PARA PODER ACCEDER AL NOMBRE DE ARCHIVO DE IMAGEN
            Institution institution = institutionService.getInstitutionById(id);
            Response response = institutionService.deleteInstitution(id);

         // SI LA INSTITUCIÓN SE ELIMINA CORRECTAMENTE, ELIMINAR TAMBIÉN EL ARCHIVO DE IMAGEN ASOCIADO
            if (response.getMessage().equals("La institución ha sido eliminada exitosamente.")) {
                String imageFilename = institution.getImage();
                if (imageFilename != null) {
                    storageService.deleteFile(imageFilename);
                }
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new Response("No se pudo eliminar la institución, el recurso no existe"));
        }
    }
    
    
    
    // AL HACER UNA SOLICITUD GET A ESTA RUTA, SE ACCEDE A LA LISTA DE TODAS LAS INSTITUCIONES DENTRO DEL DASHBOARD
    //http://localhost:8080/institutions/getAllInstitutions
    //ADMIN-WEB
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/getAllInstitutions")
    public ResponseEntity<List<Institution>> getAllInstitutions() {
        List<Institution> institutions = institutionService.getAllInstitutions();
        return ResponseEntity.ok(institutions);
    }
    
    
    
    //OBTIENE TODOS LOS USUARIOS INSTITUCIONALES DENTRO DEL DASHBOARD.
    //WEB
    //ADMIN-WEB
    //http://localhost:8080/institutions/getAllUsersInstitutions
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/getAllUsersInstitutions")
    public ResponseEntity<List<UserInstitutionDTO>> getAllUsers() {
        List<UserInstitutionDTO> userDTOs = userInstitutionService.getAllUserDTOs();
        return ResponseEntity.ok(userDTOs);
    }


    // AL HACER UNA SOLICITUD GET A ESTA RUTA, SE ACCEDE A UNA INSTITUCIÓN ESPECÍFICA POR SU ID CON STATUS TRUE
    //MOVIL
    //http://localhost:8080/institutions/getInstitutionById/12
    @GetMapping("/getInstitutionById/{id}")
    public ResponseEntity<?> getInstitution(@PathVariable Long id) {
        try {
            Institution institution = institutionService.getInstitutionByIdTrue(id);
            return ResponseEntity.ok(institution);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new Response(e.getMessage()));
        }
    }


    // AL HACER UNA SOLICITUD GET A ESTA RUTA, SE ACCEDE A LA LISTA DE TODAS LAS INSTITUCIONES CON STATUS TRUE
    //MOVIL
    //http://localhost:8080/institutions/getAllInstitutionsTrue
    @GetMapping("/getAllInstitutionsTrue")
    public ResponseEntity<List<Institution>> getAllInstitutionsTrue() {
        List<Institution> institutions = institutionService.getAllInstitutionsTrue();
        return ResponseEntity.ok(institutions);
    }
    
    
    // AL HACER UNA SOLICITUD GET A ESTA RUTA, SE ACCEDE A UNA INSTITUCIÓN ESPECÍFICA POR SU NOMBRE CON STATUS TRUE
    //MOVIL
    //http://localhost:8080/institutions/getInstitutionByName/Hospital San Juan
    @GetMapping("/getInstitutionByName/{name}")
    public ResponseEntity<?> getInstitutionByName(@PathVariable String name) {
        try {
            Institution institution = institutionService.getInstitutionByName(name);
            return ResponseEntity.ok(institution);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Response("No se encontró la institución con nombre: " + name));
        }
    }

    // AL HACER UNA SOLICITUD GET A ESTA RUTA, SE ACCEDE A UN USUARIO ESPECÍFICO POR SU ID
    //ADMIN-WEB
    // http://localhost:8080/institutions/getUserInstitutionById/12
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
    
    
    //OBTIENE TODOS LOS USUARIOS DE LA MISMA INSTITUCIÓN QUE EL USUARIO AUTENTICADO DENTRO DEL DASHBOARD.
    //ADMIN-WEB
    // http://localhost:8080/institutions/getAllSameInstitution
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/getAllSameInstitution")
    public ResponseEntity<List<UserInstitutionDTO>> getUsersFromSameInstitution(Authentication authentication) {
        String email = authentication.getName();
        List<UserInstitutionDTO> users = userInstitutionService.getUsersFromSameInstitution(email);
        return ResponseEntity.ok(users);
    }
    
    
    
    // ENDPOINT PARA ACTUALIZAR UN USUARIO DENTRO DEL DASHBOARD
    //ADMIN-WEB
    // http://localhost:8080/institutions/updateUserInstitution/12
    @PreAuthorize("hasAuthority('ADMIN')")
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
    
    
    // ENDPOINT PARA ELIMINAR UN USUARIO DENTRO DEL DASHBOARD
    //ADMIN-WEB
    //http://localhost:8080/institutions/deleteUserInstitutionById/12
    @PreAuthorize("hasAuthority('ADMIN')")
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

