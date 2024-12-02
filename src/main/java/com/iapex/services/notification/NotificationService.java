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
import com.iapex.models.patient.Patient;
import com.iapex.models.response.PageResponse;
import com.iapex.models.response.Response;
import com.iapex.models.user.UserWeb;
import com.iapex.repositories.notification.NotificationRepository;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Crear una notificación
    public void createNotification(ContactRequest contactRequest, Institution institution) {
        Notification notification = new Notification();
        notification.setContactRequest(contactRequest);
        notification.setInstitution(institution);

        // Formateador para la fecha y hora en español
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy 'a las' hh:mm a",
                new Locale("es", "ES"));

        // Formatear la fecha de solicitud
        String formattedRequestDate = contactRequest.getRequestDateTime().format(formatter);

        // Obtener nombre completo del paciente o ID si no está disponible
        String patientIdentifier;
        if (contactRequest.getPatient() != null) {
            Patient patient = contactRequest.getPatient();
            if (patient.getName() != null && !patient.getName().isEmpty()) {
                patientIdentifier = patient.getName();
                if (patient.getLastName() != null && !patient.getLastName().isEmpty()) {
                    patientIdentifier += " " + patient.getLastName();
                }
                if (patient.getSecondLastName() != null && !patient.getSecondLastName().isEmpty()) {
                    patientIdentifier += " " + patient.getSecondLastName();
                }
            } else {
                patientIdentifier = "con el ID: " + patient.getId();
            }
        } else {
            patientIdentifier = "Paciente no identificado";
        }

        // Obtener la información de contacto
        StringBuilder contactInfoBuilder = new StringBuilder();
        if (contactRequest.getPhoneNumber() != null && !contactRequest.getPhoneNumber().isEmpty()) {
            contactInfoBuilder.append("con el número ").append(contactRequest.getPhoneNumber());
        }
        if (contactRequest.getEmail() != null && !contactRequest.getEmail().isEmpty()) {
            if (contactInfoBuilder.length() > 0) {
                contactInfoBuilder.append(" y "); // Conjunción si ambos están disponibles
            }
            contactInfoBuilder.append("con el correo ").append(contactRequest.getEmail());
        }
        String contactInfo = contactInfoBuilder.toString();

        // Construir el cuerpo de la notificación
        notification.setBody(
                "Hemos recibido una nueva solicitud de contacto para el paciente " + patientIdentifier
                        + ". El interesado es " + contactRequest.getInterestedPersonName()
                        + ", " + contactInfo
                        + ". La solicitud fue recibida el " + formattedRequestDate
                        + " y está actualmente en estado \"Nueva\". Por favor, revise y gestione esta solicitud a la mayor brevedad posible.");

        notification.setSubject("Nueva solicitud de contacto para el paciente " + patientIdentifier);
        notification.setSendDate(LocalDateTime.now());
        notification.setAttended(false);

        // Guardar la notificación
        notificationRepository.save(notification);
    }

    // Obtener notificaciones por institución con paginación y filtro por estado
    public PageResponse<NotificationDTO> getNotificationsByInstitution(Long institutionId, int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Notification> notifications;
        notifications = notificationRepository.findByInstitutionId(institutionId, pageable);

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
