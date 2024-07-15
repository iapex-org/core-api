package com.iapex.scheduling;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.iapex.repository.MembershipRepository;
import com.iapex.repository.InstitutionRepository;

@Component
public class MembershipScheduler {

    private final MembershipRepository membershipRepository;
    private final InstitutionRepository institutionRepository;

    public MembershipScheduler(MembershipRepository membershipRepository, InstitutionRepository institutionRepository) {
        this.membershipRepository = membershipRepository;
        this.institutionRepository = institutionRepository;
    }

    @Scheduled(cron = "0 * * * * *") // CADA 1 MINUTO
    @Transactional
    public void scheduleTaskToUpdateExpiredMemberships() {
        System.out.println("Ejecutando tarea programada para actualizar membresías expiradas: " + LocalDateTime.now());

        int updatedCount = membershipRepository.updateExpiredMemberships(LocalDateTime.now());

        System.out.println("Membresías expiradas actualizadas: " + updatedCount);

        // Actualizar el estado de las instituciones que no tienen membresías activas
        institutionRepository.updateInstitutionStatusWithNoActiveMemberships();

        System.out.println("Estados de instituciones actualizados.");
    }
}