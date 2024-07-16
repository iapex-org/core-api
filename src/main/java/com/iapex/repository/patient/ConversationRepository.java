package com.iapex.repository.patient;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iapex.model.patient.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

}
