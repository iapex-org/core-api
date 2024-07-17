package com.iapex.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.iapex.models.ContactRequest;
import com.iapex.models.institution.Institution;

public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {
    List<ContactRequest> findByPatientInstitution(Institution institution);
}