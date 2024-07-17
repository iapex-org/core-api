package com.iapex.controller.patient;

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
import com.iapex.dto.patient.ImageDTO;
import com.iapex.dto.patient.PatientDTO;
import com.iapex.model.patient.Image;
import com.iapex.model.response.Response;
import com.iapex.model.user.UserWeb;
import com.iapex.service.files.StorageService;
import com.iapex.service.patient.PatientService;
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

    //IMPORTANTE ENVIAR LOS ARCHIVOS DESDE imageFile
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
            if (imageFiles != null) {
                // VALIDAR EL NÚMERO MÍNIMO Y MÁXIMO DE ARCHIVOS ACEPTABLES
                if (imageFiles.isEmpty()) {
                    // SI IMAGEFILES ESTÁ PRESENTE PERO VACÍO, MOSTRAR EL MENSAJE CORRESPONDIENTE
                    return ResponseEntity.badRequest().body(new Response("Debe adjuntar al menos un archivo de imagen."));
                } else if (imageFiles.size() < 8 || imageFiles.size() > 12) {
                    // SI LA CANTIDAD DE ARCHIVOS NO ESTÁ DENTRO DEL RANGO VÁLIDO, MOSTRAR EL MENSAJE CORRESPONDIENTE
                    return ResponseEntity.badRequest().body(new Response("Debe subir al menos 8 y como máximo 12 archivos de imagen."));
                }
            }

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

            // OBTENER LA INFORMACIÓN DEL USUARIO AUTENTICADO
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(401).body(new Response("Necesita iniciar sesión, como personal de la institucion para usar este recurso"));
            }
            UserWeb userWeb = (UserWeb) authentication.getPrincipal();
            String name = userWeb.getName(); 
            String lastName = userWeb.getLastName(); 
            String secondLastName = userWeb.getSecondLastName(); 

            // LLAMAR AL SERVICIO PARA REGISTRAR EL PACIENTE
            Response response = patientService.registerPatient(patientDTO, name, lastName, secondLastName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new Response("Ha ocurrido un error"));
        }
    }

    //AL HACER UN METODO GET EN ESPECIFICO SE LLAMA A ESTA RUTA PARA ACCEDER AL ARCHIVO
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

    // ENDPOINT PARA OBTENER UN PACIENTE POR SU ID NO IMPORTA SU ESTATUS
    //WEB
    //http://localhost:8080/patients/getPatientById/1
    @GetMapping("/getPatientById/{id}")
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

    // ENDPOINT PARA OBTENER TODOS LOS PACIENTES DENTRO DEL DASHBOARD NO IMPORTA SU ESTATUS
    //LISTA TODOS LOS PACIENTES
    //WEB///
    //http://localhost:8080/patients/getAllPatients    
    @GetMapping("/getAllPatients")
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        List<PatientDTO> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    // ENDPOINT PARA OBTENER TODOS LOS PACIENTES,  IDEAL PARA MOSTRAR EN LA MOVIL YA QUE SOLO SE DEBEN MOSTRAR PACIENTES NO ENCONTRADO
    //MOVIL//
    //http://localhost:8080/patients/getPatientByIdFalse    
    @GetMapping("/getAllPatientsFalse")
    public ResponseEntity<List<PatientDTO>> getAllPatientsFalse() {
        List<PatientDTO> patients = patientService.getAllPatientsFalse();
        return ResponseEntity.ok(patients);
    }

    // OBTENER LAS PACIENTES DE LA MISMA INSTITUCIÓN QUE EL USUARIO AUTENTICADO, IDEAL PARA USARLOS EN EL DASHBOARD, CUANDO UN EMPLEADO INGRESE SOLO SE LE MOSTRARA PACIENTES DE SU INSTITUCION
    //WEB
    //http://localhost:8080/patients/getPatientsByInstitution
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/getPatientsByInstitution")
    public ResponseEntity<List<PatientDTO>> getPatientsByInstitution() {
        try {
            // LLAMAR AL SERVICIO PARA OBTENER LOS PACIENTES DE LA MISMA INSTITUCIÓN
            List<PatientDTO> patients = patientService.getPatientsByAuthenticatedUser();
            return new ResponseEntity<>(patients, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ENDPOINT PARA OBTENER UN PACIENTE POR SU ID SI STATUS ES FALSE, IDEAL PARA MOSTRAR EN LA MOVIL YA QUE SOLO SE DEBEN MOSTRAR PACIENTES NO ENCONTRADOS
    //MOVIL
    //ACCEDE AL PACIENTE NO ENCONTRADO
    //http://localhost:8080/patients/getPatientByIdFalse/1    
    @GetMapping("/getPatientByIdFalse/{id}")
    public ResponseEntity<?> getPatientByIdFalse(@PathVariable Long id) {
        try {
            PatientDTO patientDTO = patientService.getPatientByIdFalse(id);
            return ResponseEntity.ok(patientDTO);
        } catch (Exception e) {
            e.printStackTrace();
            Response errorResponse = new Response("Paciente no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

}
