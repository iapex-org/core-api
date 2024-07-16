package com.iapex.controller.institution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.iapex.institution.DTO.ConversationDTO;
import com.iapex.model.Response;
import com.iapex.service.ConversationService;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    //CREAR CONVERSACION
    @PostMapping("/createConversation")
    public ResponseEntity<Response> registerConversation(@RequestBody ConversationDTO request) {
        Response response = conversationService.registerConversation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //OBTENER CONVERSACION
    @GetMapping("/getConversationById/{id}")
    public ResponseEntity<?> getConversationById(@PathVariable Long id) {
        try {
            ConversationDTO conversation = conversationService.getConversationById(id);
            return new ResponseEntity<>(conversation, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new Response(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    //OBTENER CONVERSACIONES
    @GetMapping("/getAllConversations")
    public ResponseEntity<List<ConversationDTO>> getAllConversations() {
        List<ConversationDTO> conversations = conversationService.getAllConversations();
        return new ResponseEntity<>(conversations, HttpStatus.OK);
    }
    
    //ACTUALIZAR CONVERSACION
    @PutMapping("/updateConversationById/{id}")
    public ResponseEntity<Response> updateConversationStatus(@PathVariable Long id, @RequestBody ConversationDTO request) {
        Response response = conversationService.updateById(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

