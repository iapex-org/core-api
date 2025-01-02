package com.iapex.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.iapex.dtos.notification.NotificationDTO;
import com.iapex.enums.ContactRequestStatusEnum;
import com.iapex.models.ContactRequest;
import com.iapex.models.Notification;
import com.iapex.models.institution.Institution;
import com.iapex.models.patient.Patient;
import com.iapex.models.response.PageResponse;
import com.iapex.models.response.Response;
import com.iapex.models.user.UserWeb;
import com.iapex.repositories.contactRequest.ContactRequestRepository;
import com.iapex.repositories.NotificationRepository;

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

    @Autowired
    private ContactRequestRepository contactRequestRepository;

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
                        + ". Por favor, revise y gestione esta solicitud a la mayor brevedad posible.");

        notification.setSubject("Nueva solicitud de contacto para el paciente " + patientIdentifier);
        notification.setSendDate(LocalDateTime.now());

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
                notification.getAttendingUser() != null
                        ? String.format("%s %s %s",
                                notification.getAttendingUser().getName(),
                                notification.getAttendingUser().getLastName(),
                                notification.getAttendingUser().getSecondLastName())
                        : null));

        return new PageResponse<>(notificationDTOs);
    }

    @Transactional
    public Response updateNotificationById(Long id) {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserWeb)) {
                throw new IllegalStateException("No se pudo obtener el usuario autenticado.");
            }
            UserWeb currentUser = (UserWeb) authentication.getPrincipal();
            System.out.println("Usuario actual: " + currentUser.getId() + " - " + currentUser.getName()); // Debug log

            // Buscar la notificación por ID
            Notification notification = notificationRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada con el ID: " + id));

            // Marcar la notificación como atendida
            LocalDateTime now = LocalDateTime.now();
            notification.setAttendDateTime(now);
            notification.setAttendingUser(currentUser);

            // Verificar si hay una solicitud de contacto asociada
            if (notification.getContactRequest() != null) {
                ContactRequest contactRequest = notification.getContactRequest();

                // Si la solicitud no está en estado EN_REVISION, actualizarla
                if (ContactRequestStatusEnum.NUEVA.name().equals(contactRequest.getStatus())) {
                    contactRequest.setStatus(ContactRequestStatusEnum.EN_REVISION.name());
                    contactRequest.setAttendingUser(currentUser);
                    contactRequestRepository.save(contactRequest); // Guardar cambios en la solicitud
                    System.out.println("Solicitud de contacto actualizada a EN_REVISION: " + contactRequest.getId()); // Debug
                                                                                                                      // log
                }
            }

            // Guardar la notificación actualizada
            notificationRepository.save(notification);
            System.out.println("Notificación actualizada: " + notification.getId()); // Debug log

            return new Response("El estado de la notificación ha sido actualizado correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            return new Response("Error al actualizar la notificación: " + e.getMessage());
        }
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
                notification.getAttendingUser() != null
                        ? String.format("%s %s %s",
                                notification.getAttendingUser().getName(),
                                notification.getAttendingUser().getLastName(),
                                notification.getAttendingUser().getSecondLastName())
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
