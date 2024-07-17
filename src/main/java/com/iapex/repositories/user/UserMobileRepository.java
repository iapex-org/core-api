package com.iapex.repositories.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iapex.models.user.UserMobile;

import java.util.Optional;

public interface UserMobileRepository extends JpaRepository<UserMobile, Long> {

	// MÉTODO QUE BUSCA UN USUARIO POR SU CORREO ELECTRÓNICO (EMAIL) Y DEVUELVE UN OPTIONAL<USER>.
	// EL USO DE OPTIONAL<USER> INDICA QUE EL RESULTADO PUEDE SER UN USUARIO (SI SE ENCUENTRA) O VACÍO (SI NO SE ENCUENTRA).
	Optional<UserMobile> findByEmail(String email);

}