package com.iapex.repository.institution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iapex.model.institution.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
}
