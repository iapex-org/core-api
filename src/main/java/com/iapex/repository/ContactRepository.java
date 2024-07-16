package com.iapex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iapex.model.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
}
