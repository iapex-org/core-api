package com.iapex.repositories.contactRequest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iapex.models.ContactRequest;
import com.iapex.models.institution.Institution;

@Repository
public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {
    List<ContactRequest> findByPatientInstitution(Institution institution);
}
