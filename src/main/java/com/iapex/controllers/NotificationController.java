package com.iapex.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iapex.dtos.notification.NotificationDTO;
import com.iapex.models.institution.Institution;
import com.iapex.models.response.PageResponse;
import com.iapex.models.response.Response;
import com.iapex.models.user.UserWeb;
import com.iapex.services.NotificationService;

import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Obtener notificaciones
    // http://localhost:8080/api/v1/notifications

    // Obtener notificaciones con paginación sin filtro
    // http://localhost:8080/api/v1/notifications?page=0&size=10

    // Obtener notificaciones con paginación y filtro
    // http://localhost:8080/api/v1/notifications?page=0&size=10&attended=true
    // http://localhost:8080/api/v1/notifications?page=0&size=10&attended=false
        @GetMapping
        public ResponseEntity<PageResponse<NotificationDTO>> getNotifications(
                Authentication authentication,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "5") int size) {
            
            // Obtener el usuario actual
            UserWeb currentUser = (UserWeb) authentication.getPrincipal();
            Institution institution = currentUser.getInstitution();
            
            // Obtener las notificaciones con paginación y filtro
            PageResponse<NotificationDTO> notifications = notificationService.getNotificationsByInstitution(
                    institution.getId(),
                    page,
                    size);
                    
            return ResponseEntity.ok(notifications);
        }

    // Endpoint para actualizar el estado de una notificación
    //http://localhost:8080/api/v1/notifications/17?attended=false
    //http://localhost:8080/api/v1/notifications/17?attended=true
    @PatchMapping("/{id}")
    public ResponseEntity<Response> updateNotificationStatus(
            @PathVariable Long id) {
        Response response = notificationService.updateNotificationById(id);
        return ResponseEntity.ok(response);
    }

    // Endpoint para obtener una notificación por ID
    //http://localhost:8080/api/v1/notifications/17
    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable Long id) {
        NotificationDTO notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(notification);
    }

    // Endpoint para eliminar una notificación
    //http://localhost:8080/api/v1/notifications/17
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteNotificationById(@PathVariable Long id) {
        Response response = notificationService.deleteNotificationById(id);
        return ResponseEntity.ok(response);
    }
}

