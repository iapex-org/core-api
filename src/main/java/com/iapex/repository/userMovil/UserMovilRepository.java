package com.iapex.repository.userMovil;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iapex.model.userMovil.UserMovil;

import java.util.Optional;

public interface UserMovilRepository extends JpaRepository<UserMovil, Long> {

	// MÉTODO QUE BUSCA UN USUARIO POR SU CORREO ELECTRÓNICO (EMAIL) Y DEVUELVE UN OPTIONAL<USER>.
	// EL USO DE OPTIONAL<USER> INDICA QUE EL RESULTADO PUEDE SER UN USUARIO (SI SE ENCUENTRA) O VACÍO (SI NO SE ENCUENTRA).
	Optional<UserMovil> findByEmail(String email);




}