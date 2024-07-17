package com.iapex.repository.user;

import com.iapex.model.institution.Institution;
import com.iapex.model.user.UserWeb;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserWebRepository extends JpaRepository<UserWeb, Long> {

	// ESTE MÉTODO BUSCA UNA ENTIDAD USERWEB EN LA BASE DE DATOS UTILIZANDO EL CAMPO EMAIL.
	// DEVUELVE UN OPTIONAL QUE CONTIENE LA ENTIDAD SI SE ENCUENTRA, O VACÍO SI NO SE ENCUENTRA.
	Optional<UserWeb> findByEmail(String email);
	
	// MÉTODO QUE LISTA LOS USUARIOS CON EL NOMBRE DE LA INSTITUCION
    List<UserWeb> findByInstitution(Institution institution);

}