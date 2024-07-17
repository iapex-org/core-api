package com.iapex.repository.patient;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.iapex.model.institution.Institution;
import com.iapex.model.patient.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Busca un paciente por su ID y verifica que esté activo (status true)
    Optional<Patient> findByIdPatientAndActiveTrue(Long idPatient);

    // Busca todos los pacientes que estén activos (status true)
    List<Patient> findByActiveTrue();
    
    // Busca un paciente por su nombre
    Optional<Patient> findByName(String name);

    // Busca pacientes por la institución a la que pertenecen
    List<Patient> findByHealthInstitution(Institution institution);

}
