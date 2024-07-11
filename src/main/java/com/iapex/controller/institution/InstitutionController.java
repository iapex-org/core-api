package com.iapex.controller.institution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.iapex.exceptions.InstitutionAlreadyExistsException;
import com.iapex.institution.DTO.InstitutionDTO;
import com.iapex.model.Response;
import com.iapex.model.institution.Institution;
import com.iapex.service.img.StorageService;
import com.iapex.service.institution.InstitutionService;
import org.springframework.http.MediaType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/institution")
public class InstitutionController {

    @Autowired
    private InstitutionService institutionService;
    
    @Autowired
    private StorageService storageService;

    @Autowired
    private HttpServletRequest request;
   
    
    // AL HACER UNA SOLICITUD POST A ESTA RUTA, SE CREA UNA NUEVA INSTITUCIÓN CON DATOS MULTIPART
    //http://localhost:8080/institution/createInstitution
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(value = "/createInstitution", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(@Valid @ModelAttribute InstitutionDTO request,
                                   @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                   BindingResult result) {
     // EL MÉTODO ACEPTA UN InstitutionDTO (VINCULADO AUTOMÁTICAMENTE A LOS CAMPOS DEL FORMULARIO),
     // UN MultipartFile OPCIONAL PARA LA IMAGEN, Y UN BindingResult PARA MANEJAR ERRORES DE VALIDACIÓN

     if (result.hasErrors()) {
         // SI HAY ERRORES DE VALIDACIÓN EN EL DTO, SE RECOPILAN Y SE DEVUELVEN COMO RESPUESTA
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
                 .path("/institution/")
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
    //http://localhost:8080/institution/updateInstitution/12
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/updateInstitution/{id}")
    public ResponseEntity<?> updateInstitution(
            @PathVariable Long id,
            @Valid @ModelAttribute InstitutionDTO request,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            BindingResult result) {
        
        if (result.hasErrors()) {
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
                        .path("/institution/")
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

    
    //AL HACER UN METOODO GET EN ESPECIFICO SE LLAMA A ESTA RUTA PARA ACCEDER AL ARCHIVO
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

    // AL HACER UNA SOLICITUD GET A ESTA RUTA, SE ACCEDE A UNA INSTITUCIÓN ESPECÍFICA POR SU ID
    //http://localhost:8080/institution/getInstitutionById/12
    @GetMapping("/getInstitutionById/{id}")
    public ResponseEntity<?> getInstitution(@PathVariable Long id) {
        try {
            Institution institution = institutionService.getInstitutionById(id);
            return ResponseEntity.ok(institution);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new Response(e.getMessage()));
        }
    }
    

    // AL HACER UNA SOLICITUD GET A ESTA RUTA, SE ACCEDE A LA LISTA DE TODAS LAS INSTITUCIONES
    //http://localhost:8080/institution/getAllInstitutions
    @GetMapping("/getAllInstitutions")
    public ResponseEntity<List<Institution>> getAllInstitutions() {
        List<Institution> institutions = institutionService.getAllInstitutions();
        return ResponseEntity.ok(institutions);
    }

    
    // AL HACER UNA SOLICITUD GET A ESTA RUTA, SE ACCEDE A UNA INSTITUCIÓN ESPECÍFICA POR SU NOMBRE
    //http://localhost:8080/institution/getInstitutionByName/Hospital San Juan
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
    
    //ELIMINAR POR COMPLETO UNA INSTITUCION
    //http://localhost:8080/institution/deleteInstitution/15
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
    
    
}