package com.iapex.scheduling;

import java.util.Date;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.iapex.repository.token.TokenWebRepository;
import com.iapex.repository.token.TokenMobileRepository;

@Component
public class TokenScheduler {

    private final TokenMobileRepository tokenMobileRepository;
    private final TokenWebRepository tokenWebRepository;

    // Constructor de la clase TokenScheduled
    public TokenScheduler(TokenMobileRepository tokenMobileRepository,
            TokenWebRepository tokenWebRepository) {
        this.tokenMobileRepository = tokenMobileRepository;
        this.tokenWebRepository = tokenWebRepository;
    }

    // Método programado para ejecutarse periódicamente según la expresión Cron
    // durante una vez al día a la medianoche.
    // @Scheduled(cron = "0 * * * * *") // CADA 1 MINUTO
    @Scheduled(cron = "0 0/10 * * * *") // CADA 10 MINUTOS
    public void scheduleTaskToPurgeExpiredTokens() {
        // Registro de inicio de la tarea programada+
        System.out.println("Ejecutando tarea programada para eliminar tokens expirados: " + new Date());

        // Llamada al repositorio para eliminar tokens expirados hasta la fecha actual
        tokenMobileRepository.deleteExpiredTokens(new Date());
        tokenWebRepository.deleteExpiredTokens(new Date());

        // Registro de fin de la tarea programada
        System.out.println("Tokens expirados eliminados de la base de datos.");
    }
}