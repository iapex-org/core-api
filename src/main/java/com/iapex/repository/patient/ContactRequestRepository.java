package com.iapex.repository.patient;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.iapex.model.institution.ContactRequest;
import com.iapex.model.institution.Institution;

public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {
    List<ContactRequest> findByPatientInstitution(Institution institution);
}