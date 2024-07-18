package com.iapex.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.iapex.dtos.patient.ImageDTO;
import com.iapex.dtos.patient.PatientDTO;
import com.iapex.models.patient.Image;
import com.iapex.models.response.Response;
import com.iapex.models.user.UserWeb;
import com.iapex.services.PatientService;
import com.iapex.services.files.StorageService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private HttpServletRequest request;

	 // Obtener todos los pacientes
	 // SUPER_ADMIN: En un futuro, si se necesita tener el control de todos los pacientes de todas las instituciones, se utilizaría 
     // este endpoint, ya que lista tanto pacientes encontrados omo no encontrados
	 @GetMapping
	 public ResponseEntity<List<PatientDTO>> getAllPatients() {
	     List<PatientDTO> patients = patientService.getAllPatients();
	     return ResponseEntity.ok(patients);
	 }


	// Obtener un paciente por su ID
	// USER_WEB: Usado en la web cuando se accede a un paciente, se cargan los datos de su ID
	@GetMapping("/{id}")
	public ResponseEntity<?> getPatientById(@PathVariable Long id) {
	    try {
	        PatientDTO patientDTO = patientService.getPatientById(id);
	        return ResponseEntity.ok(patientDTO);
	    } catch (Exception e) {
	        e.printStackTrace();
	        Response errorResponse = new Response("Paciente no encontrado");
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
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

    // Pensado para ser usado en la app movil, ya que solo se deben mostrar los
    // pacientes no encontrados (aún activos)
    // Obtener todos los pacientes activos
    @GetMapping("/active")
    public ResponseEntity<List<PatientDTO>> getAllPatientsTrue() {
        List<PatientDTO> patients = patientService.getAllPatientsTrue();
        return ResponseEntity.ok(patients);
    }
    
    

    // Pensado para ser usado en la app móvil, especificamente al momento de abrir
    // un resultado especifico de un paciente
    // Obtener un paciente no encontrado (aun activo) por su ID
    @GetMapping("/{id}/active")
    public ResponseEntity<?> getPatientByIdTrue(@PathVariable Long id) {
        try {
            PatientDTO patientDTO = patientService.getPatientByIdTrue(id);
            return ResponseEntity.ok(patientDTO);
        } catch (Exception e) {
            e.printStackTrace();
            Response errorResponse = new Response("Paciente no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }    
    
    // Pensado para ser usado en la app web, ya que solo muestra los pacientes que
    // corresponden a la institución de la que forma parte el usuario autenticado
    // Obtener los pacientes de la misma institución que el usuario autenticado
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/current-user/institution")
    public ResponseEntity<List<PatientDTO>> getPatientsByInstitution() {
        try {
            // Llamar al servicio para obtener los pacientes de la misma institución
            List<PatientDTO> patients = patientService.getPatientsByAuthenticatedUser();
            return new ResponseEntity<>(patients, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Impotante: Enviar archivos desde "imageFile"
    // Crear un paciente
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerPatient(
            @Valid @ModelAttribute PatientDTO patientDTO,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) List<MultipartFile> imageFiles) {

        if (result.hasErrors()) {
            // Manejo de errores de validación
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }

        try {

            List<Image> images = new ArrayList<>();

            // Procesar archivos de imagen si se proporcionaron
            if (imageFiles != null) {
                // Validar la cantidad de archivos de imagen
                if (imageFiles.isEmpty()) {
                    // Si imageFiles está presnte, pero vacío, mostrar el mensaje correspondiente
                    return ResponseEntity.badRequest()
                            .body(new Response("Debe adjuntar al menos un archivo de imagen."));
                } else if (imageFiles.size() < 8 || imageFiles.size() > 12) {
                    // Si la cantidad de archivos de imagen no está entre 8 y 12, mostrar el mensaje
                    // correspondiente
                    return ResponseEntity.badRequest()
                            .body(new Response("Debe subir al menos 8 y como máximo 12 archivos de imagen."));
                }
            }

            // Procesar archivos de imagen si se proporcionaron
            if (imageFiles != null && !imageFiles.isEmpty()) {
                for (MultipartFile imageFile : imageFiles) {
                    if (!imageFile.isEmpty()) {
                        // Guardar el archivo de imagen en el sistema de archivos
                        String originalFilename = imageFile.getOriginalFilename();
                        String uniqueFilename = storageService.generateUniqueFilename(originalFilename);
                        String storedFilename = storageService.saveFile(imageFile, uniqueFilename);

                        // Construir la URL completa de la imagen
                        String host = request.getRequestURL().toString().replace(request.getRequestURI(), "");
                        String imageUrl = ServletUriComponentsBuilder
                                .fromHttpUrl(host)
                                .path("/api/v1/patients/images/") // Ruta de las imágenes de los pacientes
                                .path(storedFilename)
                                .toUriString();

                        // Guardar la imagen en la lista de imágenes
                        Image image = new Image();
                        image.setImage(storedFilename);
                        image.setImageUrl(imageUrl);
                        images.add(image);
                    }
                }
            }

            // Convertir la lista de Image a ImageDTO
            List<ImageDTO> imageDTOs = images.stream()
                    .map(image -> new ImageDTO(image.getId(), image.getImage(), image.getImageUrl()))
                    .collect(Collectors.toList());

            // Establecer las imágenes en el DTO del paciente
            patientDTO.setImages(imageDTOs);

            // Obtener la autenticación del usuario
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || authentication.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(401).body(new Response(
                        "Necesita iniciar sesión como personal de la institucion para usar este recurso"));
            }
            
            // Obtener el usuario autenticado
            UserWeb authenticatedUser = (UserWeb) authentication.getPrincipal();


            // Llamar al servicio para registrar el paciente
            Response response = patientService.registerPatient(patientDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new Response("Ha ocurrido un error, verifica que la institucion exista"));
        }
    }
    
    @PutMapping(value = "updatePatient/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePatient(@PathVariable Long id,
                                           @Valid @ModelAttribute PatientDTO patientDTO,
                                           BindingResult result,
                                           @RequestParam(value = "imageFile", required = false) List<MultipartFile> imageFiles) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() 
                    || authentication.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(
                        "Necesita iniciar sesión como personal de la institución para usar este recurso"));
            }

            List<ImageDTO> imageDTOs = new ArrayList<>();

            if (imageFiles != null && !imageFiles.isEmpty()) {
                if (imageFiles.size() < 8 || imageFiles.size() > 12) {
                    return ResponseEntity.badRequest()
                            .body(new Response("Debe subir entre 8 y 12 archivos de imagen."));
                }

                for (MultipartFile imageFile : imageFiles) {
                    if (!imageFile.isEmpty()) {
                        String originalFilename = imageFile.getOriginalFilename();
                        String uniqueFilename = storageService.generateUniqueFilename(originalFilename);
                        String storedFilename = storageService.saveFile(imageFile, uniqueFilename);

                        String host = request.getRequestURL().toString().replace(request.getRequestURI(), "");
                        String imageUrl = ServletUriComponentsBuilder
                                .fromHttpUrl(host)
                                .path("/api/v1/patients/images/")
                                .path(storedFilename)
                                .toUriString();

                        imageDTOs.add(new ImageDTO(null, storedFilename, imageUrl));
                    }
                }

                patientDTO.setImages(imageDTOs);
            }

            Response response = patientService.updatePatient(id, patientDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response("Ha ocurrido un error al actualizar el paciente: " + e.getMessage()));
        }
    }
}

