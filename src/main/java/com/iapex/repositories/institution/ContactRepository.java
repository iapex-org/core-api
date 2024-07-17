package com.iapex.repositories.institution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iapex.models.institution.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
}
