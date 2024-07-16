package com.iapex.repository.patient;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iapex.model.institution.Institution;
import com.iapex.model.patient.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByPatientInstitution(Institution institution);
}
