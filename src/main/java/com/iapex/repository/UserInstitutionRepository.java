package com.iapex.repository;

import com.iapex.model.institution.UserInstitution;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInstitutionRepository extends JpaRepository<UserInstitution, Long> {

	// ESTE MÉTODO BUSCA UNA ENTIDAD USERINSTITUTION EN LA BASE DE DATOS UTILIZANDO EL CAMPO EMAIL.
	// DEVUELVE UN OPTIONAL QUE CONTIENE LA ENTIDAD SI SE ENCUENTRA, O VACÍO SI NO SE ENCUENTRA.
	Optional<UserInstitution> findByEmail(String email);



}
