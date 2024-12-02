package com.iapex.services.notification;

import org.springframework.util.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.iapex.dtos.notification.NotificationDTO;
import com.iapex.models.ContactRequest;
import com.iapex.models.institution.Institution;
import com.iapex.models.notification.Notification;
import com.iapex.models.response.PageResponse;
import com.iapex.models.response.Response;
import com.iapex.models.user.UserWeb;
import com.iapex.repositories.notification.NotificationRepository;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Crear una notificación
    public void createNotification(ContactRequest contactRequest, Institution institution) {
        Notification notification = new Notification();
        notification.setContactRequest(contactRequest);
        notification.setInstitution(institution); // Asignar la institución del paciente
        notification.setSubject("Nueva solicitud de contacto");
        notification.setBody(
                "Tiene una nueva solicitud relacionada con el paciente: " + contactRequest.getPatient().getName());
        notification.setSendDate(LocalDateTime.now());
        notification.setAttended(false);

        notificationRepository.save(notification);
    }

    // Obtener notificaciones por institución con paginación y filtro por estado
    public PageResponse<NotificationDTO> getNotificationsByInstitution(Long institutionId, Boolean attended, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Notification> notifications;
        if (ObjectUtils.isEmpty(attended)) {
            notifications = notificationRepository.findByInstitutionId(institutionId, pageable);
        } else {
            notifications = notificationRepository.findByInstitutionIdAndAttended(institutionId, attended, pageable);
        }

        Page<NotificationDTO> notificationDTOs = notifications.map(notification -> new NotificationDTO(
                notification.getId(),
                notification.getContactRequest().getId(),
                notification.getSubject(),
                notification.getBody(),
                notification.getSendDate(),
                notification.getAttendDateTime(),
                notification.isAttended(),
                notification.getAttendedBy() != null 
                    ? String.format("%s %s %s",
                        notification.getAttendedBy().getName(),
                        notification.getAttendedBy().getLastName(),
                        notification.getAttendedBy().getSecondLastName())
                    : null));

        return new PageResponse<>(notificationDTOs);
    }

    @Transactional
    public Response updateNotificationById(Long id, boolean attended) {
        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserWeb)) {
            throw new IllegalStateException("No se pudo obtener el usuario autenticado.");
        }
        UserWeb currentUser = (UserWeb) authentication.getPrincipal();
        System.out.println("Current user: " + currentUser.getId() + " - " + currentUser.getName()); // Debug log
        
        // Find notification
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada con el ID: " + id));
        
        // Update notification
        notification.setAttended(attended);
        if (attended) {
            LocalDateTime now = LocalDateTime.now();
            notification.setAttendDateTime(now);
            notification.setAttendedBy(currentUser);
            
            // Debug logs
            System.out.println("Setting attendDateTime: " + now);
            System.out.println("Setting attendedBy: " + currentUser.getId());
        } else {
            notification.setAttendDateTime(null);
            notification.setAttendedBy(null);
        }
        
        // Save and verify
        Notification savedNotification = notificationRepository.save(notification);
        System.out.println("Saved notification - attendedBy: " + 
            (savedNotification.getAttendedBy() != null ? savedNotification.getAttendedBy().getId() : "null") + 
            ", attendDateTime: " + savedNotification.getAttendDateTime());
        
        return new Response("El estado de la notificación ha sido actualizado correctamente.");
    }


    // Obtener una notificación por ID
    public NotificationDTO getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada con el ID: " + id));

        return new NotificationDTO(
                notification.getId(),
                notification.getContactRequest().getId(),
                notification.getSubject(),
                notification.getBody(),
                notification.getSendDate(),
                notification.getAttendDateTime(),
                notification.isAttended(),
                notification.getAttendedBy() != null 
                    ? String.format("%s %s %s",
                        notification.getAttendedBy().getName(),
                        notification.getAttendedBy().getLastName(),
                        notification.getAttendedBy().getSecondLastName())
                    : null);
    }

    // Eliminar una notificación por ID
    public Response deleteNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada con el ID: " + id));

        notificationRepository.delete(notification);

        return new Response("La notificación ha sido eliminada correctamente.");
    }
}
