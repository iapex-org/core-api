package com.iapex.controller.institution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import com.iapex.model.Response;
import com.iapex.model.institution.Institution;
import com.iapex.service.img.StorageService;
import com.iapex.service.institution.InstitutionService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;

import java.util.List;

@RestController
@RequestMapping("/institutions")
public class InstitutionController {

    @Autowired
    private InstitutionService institutionService;
    
    @Autowired
    private StorageService storageService;

    @Autowired
    private HttpServletRequest request;
    

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
    @GetMapping("/getAllInstitutions")
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
    
}