package com.iapex.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iapex.models.institution.Institution;
import com.iapex.models.patient.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Busca un paciente por su ID y verifica que esté activo (status true)
    Optional<Patient> findByIdAndActiveTrue(Long id);

    // Busca un paciente por su ID y verifica que esté false (status false)
    Optional<Patient> findByIdAndActiveFalse(Long id);

    // Busca todos los pacientes que estén activos (status true)
    List<Patient> findByActiveTrue();

    // Busca todos los pacientes que estén inactivos (status false)
    List<Patient> findByActiveFalse();
    
    // Busca un paciente por su nombre
    Optional<Patient> findByName(String name);

    // Busca pacientes por la institución a la que pertenecen
    List<Patient> findByInstitution(Institution institution);
    
    //Este método buscará pacientes que estén activos y cuya institución asociada también esté activa.
    List<Patient> findByActiveTrueAndInstitution_ActiveTrue();
}