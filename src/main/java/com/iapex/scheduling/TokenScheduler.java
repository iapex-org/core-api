package com.iapex.scheduling;

import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.iapex.repository.token.TokenInstitutionRepository;
import com.iapex.repository.token.TokenMovilRepository;


@Component
public class TokenScheduler {

    private final TokenMovilRepository tokenMovilRepository;
    private final TokenInstitutionRepository tokenInstitutionRepository;

    // Constructor de la clase TokenScheduled
    public TokenScheduler(TokenMovilRepository tokenMovilRepository, TokenInstitutionRepository tokenInstitutionRepository) {
        this.tokenMovilRepository = tokenMovilRepository;
        this.tokenInstitutionRepository = tokenInstitutionRepository;
    }



    // Método programado para ejecutarse periódicamente según la expresión Cron durante una vez al día a la medianoche.
    //@Scheduled(cron = "0 * * * * *") // CADA 1 MINUTO
    @Scheduled(cron = "0 0/10 * * * *") // CADA 10 MINUTOS
    public void scheduleTaskToPurgeExpiredTokens() {
        // Registro de inicio de la tarea programada+
        System.out.println("Ejecutando tarea programada para eliminar tokens expirados: " + new Date());

        // Llamada al repositorio para eliminar tokens expirados hasta la fecha actual
        tokenMovilRepository.deleteExpiredTokens(new Date());
        tokenInstitutionRepository.deleteExpiredTokens(new Date());


        // Registro de fin de la tarea programada
        System.out.println("Tokens expirados eliminados de la base de datos.");
    }
}

