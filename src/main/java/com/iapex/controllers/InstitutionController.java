package com.iapex.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.iapex.dtos.InstitutionDTO;
import com.iapex.exceptions.InstitutionAlreadyExistsException;
import com.iapex.models.institution.Institution;
import com.iapex.models.response.Response;
import com.iapex.models.user.UserWeb;
import com.iapex.services.InstitutionService;
import com.iapex.services.files.StorageService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/institutions")
public class InstitutionController {

	
	
    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private HttpServletRequest request;

    // Obtener todas las instituciones
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<List<InstitutionDTO>> getAllInstitutions() {
        List<InstitutionDTO> institutions = institutionService.getAllInstitutions();
        return ResponseEntity.ok(institutions);
    }
    
    // Obtener institución por ID sin importar su estado
    @GetMapping("/getInstitutionById/{id}")
    public ResponseEntity<?> getInstitutionById(@PathVariable Long id) {
        try {
            Institution institution = institutionService.getInstitutionById(id);
            return ResponseEntity.ok(institution);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response("No se encontró la institución con ID: " + id));
        }
    }
    
    // Obtener el nombre de todas las instituciones con active en TRUE
    @GetMapping("/active-institution-names")
    public ResponseEntity<List<String>> getActiveInstitutionNames() {
        List<String> activeInstitutionNames = institutionService.getActiveInstitutionNames();
        return ResponseEntity.ok(activeInstitutionNames);
    }

    // Obtener todas las instituciones con status TRUE
    @GetMapping("/activated")
    public ResponseEntity<List<InstitutionDTO>> getAllInstitutionsTrue() {
        List<InstitutionDTO> institutions = institutionService.getAllInstitutionsTrue();
        return ResponseEntity.ok(institutions);
    }

    // Obtener institución por ID con status TRUE, es decir cuando la institucion esta activada
    @GetMapping("/{id}/activated")
    public ResponseEntity<?> getInstitution(@PathVariable Long id) {
        try {
            Institution institution = institutionService.getInstitutionByIdTrue(id);
            return ResponseEntity.ok(institution);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response("No se encontró la institución con ID: " + id));
        }
    }

    // Obtener todas las instituciones por su nombre con status true
    @GetMapping("/{name}")
    public ResponseEntity<?> getInstitutionByName(@PathVariable String name) {
        try {
            Institution institution = institutionService.getInstitutionByName(name);
            return ResponseEntity.ok(institution);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response("No se encontró la institución con nombre, solo se mostraran instittuciones activas: " + name));
        }
    }
    
    // Acceder a la imagen del paciente por su nombre de archivo
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Resource file = storageService.loadAsResource(filename);
            String contentType = Files.probeContentType(file.getFile().toPath());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(file);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    

    // Obtener la institución del usuario autenticado
    @PreAuthorize("hasAuthority('USER_WEB')")
    @GetMapping("/current-user")
    public ResponseEntity<?> getInstitutionByUser() {
        try {
            // Obtener información del usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserWeb userWeb = (UserWeb) authentication.getPrincipal();

            // Obtener la institución del usuario autenticado
            InstitutionDTO institutionDTO = institutionService.getInstitutionDTOByUser(userWeb);

            return ResponseEntity.ok(institutionDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response("Error al obtener la institución del usuario autenticado: " + e.getMessage()));
        }
    }

    // Crear una nueva institución
    //imageFile campo para archivos
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createInstitution(
            @Valid @ModelAttribute InstitutionDTO request,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        // Manejo de errores de validación del DTO
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            // Procesamiento de la imagen si se proporciona
            if (imageFile != null && !imageFile.isEmpty()) {
                String originalFilename = imageFile.getOriginalFilename();
                String uniqueFilename = storageService.generateUniqueFilename(originalFilename);
                String storedFilename = storageService.saveFile(imageFile, uniqueFilename);

                String host = this.request.getRequestURL().toString().replace(this.request.getRequestURI(), "");
                String imageUrl = ServletUriComponentsBuilder
                        .fromHttpUrl(host)
                        .path("/api/v1/institutions/images/")  // Nota el cambio aquí
                        .path(storedFilename)
                        .toUriString();

                request.setImage(storedFilename);
                request.setImageUrl(imageUrl);
            }

            // Llamada al servicio para registrar la institución
            Response response = institutionService.register(request);
            return ResponseEntity.ok(response);
        } catch (InstitutionAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(new Response(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new Response("Ha ocurrido un error: " + e.getMessage()));
        }
    }

    // Actualizar una institución
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateInstitution(
            @PathVariable Long id,
            @Valid @ModelAttribute InstitutionDTO request,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        // Manejo de errores de validación del DTO
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            // Obtener la institución actual para gestionar la imagen anterior si es
            // necesario
            Institution institution = institutionService.getInstitutionById(id);
            String oldImageFilename = institution.getImage();

            // Procesamiento de la nueva imagen si se proporciona
            if (imageFile != null && !imageFile.isEmpty()) {
                String originalFilename = imageFile.getOriginalFilename();
                String uniqueFilename = storageService.generateUniqueFilename(originalFilename);
                String storedFilename = storageService.saveFile(imageFile, uniqueFilename);

                String host = this.request.getRequestURL().toString().replace(this.request.getRequestURI(), "");
                String imageUrl = ServletUriComponentsBuilder
                        .fromHttpUrl(host)
                        .path("/api/v1/institutions/images/") 
                        .path(storedFilename)
                        .toUriString();

                request.setImage(storedFilename);
                request.setImageUrl(imageUrl);

                // Eliminar la imagen anterior si existía
                if (oldImageFilename != null) {
                    storageService.deleteFile(oldImageFilename);
                }
            }

            // Llamada al servicio para actualizar la institución
            Response response = institutionService.updateInstitution(id, request);
            return ResponseEntity.ok(response);
        } catch (InstitutionAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(new Response(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new Response("Ha ocurrido un error: " + e.getMessage()));
        }
    }

    // Eliminar una institución
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInstitution(@PathVariable Long id) {
        try {
            // Obtener la institución antes de eliminarla para acceder al nombre del archivo
            // de imagen
            Institution institution = institutionService.getInstitutionById(id);
            Response response = institutionService.deleteInstitution(id);

            // Si la institución se elimina correctamente, eliminar también el archivo de
            // imagen asociado
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
}
