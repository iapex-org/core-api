package com.iapex.controller.institution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.iapex.institution.DTO.ImageDTO;
import com.iapex.institution.DTO.PatientDTO;
import com.iapex.model.Response;
import com.iapex.model.institution.Image;
import com.iapex.service.img.StorageService;
import com.iapex.service.institution.PatientService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private HttpServletRequest request;


@PostMapping(value = "/createPatient", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<?> registerPatient(
        @Valid @ModelAttribute PatientDTO patientDTO,
        BindingResult result,
        @RequestParam(value = "imageFile", required = false) List<MultipartFile> imageFiles) {

    if (result.hasErrors()) {
        // MANEJO DE ERRORES DE VALIDACIÓN
        Map<String, String> errors = result.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        return ResponseEntity.badRequest().body(errors);
    }

    try {
        List<Image> images = new ArrayList<>();

        // PROCESAR CADA ARCHIVO DE IMAGEN SI SE PROPORCIONARON
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile imageFile : imageFiles) {
                if (!imageFile.isEmpty()) {
                    // GUARDAR Y OBTENER NOMBRES ÚNICOS DE ARCHIVOS
                    String originalFilename = imageFile.getOriginalFilename();
                    String uniqueFilename = storageService.generateUniqueFilename(originalFilename);
                    String storedFilename = storageService.saveFile(imageFile, uniqueFilename);

                    // CONSTRUIR URL COMPLETA PARA ACCEDER A LA IMAGEN
                    String host = request.getRequestURL().toString().replace(request.getRequestURI(), "");
                    String imageUrl = ServletUriComponentsBuilder
                            .fromHttpUrl(host)
                            .path("/patients/")  // ASEGÚRATE DE AJUSTAR LA RUTA SEGÚN TU CONFIGURACIÓN
                            .path(storedFilename)
                            .toUriString();

                    // GUARDAR LA IMAGEN EN LA LISTA DE IMÁGENES
                    Image image = new Image();
                    image.setImage(storedFilename); // NOMBRE DEL ARCHIVO ALMACENADO
                    image.setImageUrl(imageUrl);   // URL COMPLETA DE LA IMAGEN
                    images.add(image);
                }
            }
        }

        // CONVERTIR LA LISTA DE Image A ImageDTO
        List<ImageDTO> imageDTOs = images.stream()
                .map(image -> new ImageDTO(image.getIdImage(), image.getImage(), image.getImageUrl()))
                .collect(Collectors.toList());

        // ESTABLECER LAS IMÁGENES CONVERTIDAS EN EL DTO DEL PACIENTE
        patientDTO.setImages(imageDTOs);

        // LLAMAR AL SERVICIO PARA REGISTRAR EL PACIENTE
        Response response = patientService.registerPatient(patientDTO);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError().body(new Response("Ha ocurrido un error: " + e.getMessage()));
    }
}

	//AL HACER UN METOODO GET EN ESPECIFICO SE LLAMA A ESTA RUTA PARA ACCEDER AL ARCHIVO
	@GetMapping("/{filename:.+}")
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
	
    // ENDPOINT PARA OBTENER UN PACIENTE POR SU ID
	    @GetMapping("/getPatientById/{id}")
	    public ResponseEntity<?> getPatientById(@PathVariable Long id) {
	        try {
	            PatientDTO patientDTO = patientService.getPatientById(id);
	            return ResponseEntity.ok(patientDTO);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return ResponseEntity.notFound().build();
	        }
	    }
	
	    
	 // ENDPOINT PARA OBTENER TODOS LOS PACIENTES
	    @GetMapping("/getAllPatients")
	    public ResponseEntity<List<PatientDTO>> getAllPatients() {
	        List<PatientDTO> patients = patientService.getAllPatients();
	        return ResponseEntity.ok(patients);
	    }
	    
	    
	 // ENDPOINT PARA OBTENER UN PACIENTE POR SU ID SI STATUS ES FALSE
	    @GetMapping("/getPatientByIdFalse/{id}")
	    public ResponseEntity<?> getPatientByIdFalse(@PathVariable Long id) {
	        try {
	            PatientDTO patientDTO = patientService.getPatientByIdFalse(id);
	            return ResponseEntity.ok(patientDTO);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return ResponseEntity.notFound().build();
	        }
	    }
	    
	    
	  // ENDPOINT PARA OBTENER TODOS LOS PACIENTES
	    @GetMapping("/getAllPatientsFalse")
	    public ResponseEntity<List<PatientDTO>> getAllPatientsFalse() {
	        List<PatientDTO> patients = patientService.getAllPatientsFalse();
	        return ResponseEntity.ok(patients);
	    }
	
	    
	}
