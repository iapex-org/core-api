package com.iapex.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iapex.dto.TokenDTO;
import com.iapex.model.Token;
import com.iapex.repository.TokenRepository;


@Service
public class TokenService {
    
    @Autowired
    private TokenRepository tokenRepository;
    
    public List<TokenDTO> getAllTokens() {
        return tokenRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public Optional<TokenDTO> getTokenById(Long id) {
        return tokenRepository.findById(id).map(this::mapToDto);
    }
    
    public void deleteTokenById(Long id) {
        tokenRepository.deleteById(id);
    }
    
    public void deleteTokensByUserId(Long userId) {
        List<Token> tokens = tokenRepository.findByUser_IdUser(userId);
        tokenRepository.deleteAll(tokens);
    }
    
    private TokenDTO mapToDto(Token token) {
    	TokenDTO dto = new TokenDTO();
        dto.setToken(token.getToken());
        dto.setLoggedOut(token.isLoggedOut());
        dto.setUserId(token.getUser().getIdUser());
        return dto;
    }
}

