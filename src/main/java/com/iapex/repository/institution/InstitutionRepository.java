package com.iapex.repository.institution;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.iapex.model.institution.Institution;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    // ESTE MÉTODO BUSCA UNA ENTIDAD INSTITUTION EN LA BASE DE DATOS UTILIZANDO EL CAMPO EMAIL.
    // DEVUELVE UN OPTIONAL QUE CONTIENE LA ENTIDAD SI SE ENCUENTRA, O VACÍO SI NO SE ENCUENTRA.
    Optional<Institution> findByEmail(String email);

    // ESTE MÉTODO BUSCA UNA ENTIDAD INSTITUTION EN LA BASE DE DATOS UTILIZANDO EL CAMPO NAME.
    // DEVUELVE UN OPTIONAL QUE CONTIENE LA ENTIDAD SI SE ENCUENTRA, O VACÍO SI NO SE ENCUENTRA.
    Optional<Institution> findByName(String name);
    
    // ESTE MÉTODO BUSCA LAS INSTITUCIONES CON STATUS TRUE.
    List<Institution> findByStatusTrue();
    
    // ESTE MÉTODO BUSCA LAS INSTITUCIONES POR SU NOMBRE CON STATUS TRUE.
    Optional<Institution> findByNameAndStatusTrue(String name);

    // ESTE MÉTODO BUSCA LAS INSTITUCIONES POR SU ID CON STATUS TRUE.
    Optional<Institution> findByIdAndStatusTrue(Long idInstitution);

    
    @Modifying
    @Query("UPDATE Institution i SET i.status = false WHERE i.id NOT IN (SELECT DISTINCT m.institution.id FROM Membership m WHERE m.status = true)")
    void updateInstitutionStatusWithNoActiveMemberships();

}