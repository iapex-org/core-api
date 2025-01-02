package com.iapex.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.iapex.models.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Notificaciones no atendidas: aquellas donde attendingUser es null
    List<Notification> findByInstitutionIdAndAttendingUserIsNull(Long institutionId);

    // Notificaciones atendidas: aquellas donde attendingUser no es null
    Page<Notification> findByInstitutionIdAndAttendingUserIsNotNull(Long institutionId, Pageable pageable);

    // Todas las notificaciones de una institución
    Page<Notification> findByInstitutionId(Long institutionId, Pageable pageable);

    // Buscar notificación asociada a una solicitud de contacto específica
    Optional<Notification> findByContactRequestId(Long contactRequestId);

}