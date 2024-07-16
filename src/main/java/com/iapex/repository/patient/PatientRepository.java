package com.iapex.repository.patient;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iapex.model.patient.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

	// ESTE MÉTODO BUSCA LAS PACIENTES POR SU ID CON STATUS TRUE.
    Optional<Patient> findByIdPatientAndStatusFalse(Long idPatient);

    // ESTE MÉTODO BUSCA LOS PACIENTES CON STATUS TRUE.
    List<Patient> findByStatusFalse();
    
    Optional<Patient> findByName(String name);

}
