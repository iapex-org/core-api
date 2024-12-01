package com.iapex.services.notification;

import org.springframework.util.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.iapex.dtos.notification.NotificationDTO;
import com.iapex.models.ContactRequest;
import com.iapex.models.institution.Institution;
import com.iapex.models.notification.Notification;
import com.iapex.models.response.Response;
import com.iapex.repositories.notification.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Crear una notificación
    public void createNotification(ContactRequest request, Institution institution) {
        Notification notification = new Notification();
        notification.setContactRequest(request);
        notification.setRegion(institution.getDirection().getCity());
        notification.setInstitution(institution);
        notification.setAttended(false);
        notificationRepository.save(notification);
    }

    // Obtener notificaciones por institución con paginación y filtro por estado
    public Page<NotificationDTO> getNotificationsByInstitution(Long institutionId, Boolean viewed, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Notification> notifications;
        if (ObjectUtils.isEmpty(viewed)) {
            // Recuperar todas las notificaciones si `attended` es null
            notifications = notificationRepository.findByInstitutionId(institutionId, pageable);
        } else {
            // Recuperar las notificaciones filtradas por estado
            notifications = notificationRepository.findByInstitutionIdAndAttended(institutionId, viewed, pageable);
        }

        return notifications.map(notification -> new NotificationDTO(
                notification.getId(),
                notification.getContactRequest().getId(),
                notification.getContactRequest().getMissingPersonName(),
                notification.getContactRequest().getInterestedPersonName(),
                notification.getContactRequest().getRequestDateTime(),
                notification.getRegion(),
                notification.isAttended()));
    }

    public Response updateNotificationById(Long id, boolean attended) {
        // Buscar la notificación por ID
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException("Notificación no encontrada con el ID: " + id));

        // Actualizar el estado
        notification.setAttended(attended);
        notificationRepository.save(notification);

        // Devolver un mensaje indicando que el estado ha sido actualizado
        return new Response("El estado de la notificación ha sido actualizado correctamente.");
    }

    public NotificationDTO getNotificationById(Long id) {
        // Buscar la notificación por ID
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException("Notificación no encontrada con el ID: " + id));

        // Devolver la notificación
        return new NotificationDTO(
                notification.getId(),
                notification.getContactRequest().getId(),
                notification.getContactRequest().getMissingPersonName(),
                notification.getContactRequest().getInterestedPersonName(),
                notification.getContactRequest().getRequestDateTime(),
                notification.getRegion(),
                notification.isAttended());
    }

    public Response deleteNotificationById(Long id) {
        // Buscar la notificación por ID
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException("Notificación no encontrada con el ID: " + id));

        // Eliminar la notificación
        notificationRepository.delete(notification);

        // Devolver un mensaje indicando que la notificación ha sido eliminada
        return new Response("La notificación ha sido eliminada correctamente.");
    }
}
