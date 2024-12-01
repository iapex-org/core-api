package com.iapex.repositories.notification;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iapex.models.notification.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByInstitutionIdAndAttendedFalse(Long institutionId);

    Page<Notification> findByInstitutionIdAndAttended(Long institutionId, Boolean attended, Pageable pageable);

    Page<Notification> findByInstitutionId(Long institutionId, Pageable pageable);

}
