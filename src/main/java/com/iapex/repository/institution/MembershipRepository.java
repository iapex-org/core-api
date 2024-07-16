package com.iapex.repository.institution;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.iapex.model.institution.Institution;
import com.iapex.model.institution.Membership;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
	
    Optional<Membership> findByInstitutionAndStatus(Institution institution, boolean status);
    
    @Modifying
    @Query("UPDATE Membership m SET m.status = false WHERE m.endDate < :currentDateTime AND m.status = true")
    int updateExpiredMemberships(@Param("currentDateTime") LocalDateTime currentDateTime);
}
