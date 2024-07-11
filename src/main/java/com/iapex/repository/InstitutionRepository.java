package com.iapex.repository;

import com.iapex.model.institution.Institution;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    // ESTE MÉTODO BUSCA UNA ENTIDAD INSTITUTION EN LA BASE DE DATOS UTILIZANDO EL CAMPO EMAIL.
    // DEVUELVE UN OPTIONAL QUE CONTIENE LA ENTIDAD SI SE ENCUENTRA, O VACÍO SI NO SE ENCUENTRA.
    Optional<Institution> findByEmail(String email);

    // ESTE MÉTODO BUSCA UNA ENTIDAD INSTITUTION EN LA BASE DE DATOS UTILIZANDO EL CAMPO NAME.
    // DEVUELVE UN OPTIONAL QUE CONTIENE LA ENTIDAD SI SE ENCUENTRA, O VACÍO SI NO SE ENCUENTRA.
    Optional<Institution> findByName(String name);
    


}
