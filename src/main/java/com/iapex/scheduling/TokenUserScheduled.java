package com.iapex.scheduling;

import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.iapex.repository.token.TokenInstitutionRepository;
import com.iapex.repository.token.TokenRepository;


@Component
public class TokenUserScheduled {

    private final TokenRepository tokenRepository;
    private final TokenInstitutionRepository tokenInstitutionRepository;

    // Constructor de la clase TokenScheduled
    public TokenUserScheduled(TokenRepository tokenRepository, TokenInstitutionRepository tokenInstitutionRepository) {
        this.tokenRepository = tokenRepository;
        this.tokenInstitutionRepository = tokenInstitutionRepository;
    }



    // Método programado para ejecutarse periódicamente según la expresión Cron durante una vez al día a la medianoche.
    @Scheduled(cron = "0 * * * * *") // CADA 1 MINUTO
    public void scheduleTaskToPurgeExpiredTokens() {
        // Registro de inicio de la tarea programada+
        System.out.println("Ejecutando tarea programada para eliminar tokens expirados: " + new Date());

        // Llamada al repositorio para eliminar tokens expirados hasta la fecha actual
        tokenRepository.deleteExpiredTokens(new Date());
        tokenInstitutionRepository.deleteExpiredTokens(new Date());


        // Registro de fin de la tarea programada
        System.out.println("Tokens expirados eliminados de la base de datos.");
    }
}

