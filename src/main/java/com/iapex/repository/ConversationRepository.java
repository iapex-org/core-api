package com.iapex.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iapex.model.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

}
