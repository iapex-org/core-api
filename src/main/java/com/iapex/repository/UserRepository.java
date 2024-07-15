package com.iapex.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iapex.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	// MÉTODO QUE BUSCA UN USUARIO POR SU CORREO ELECTRÓNICO (EMAIL) Y DEVUELVE UN OPTIONAL<USER>.
	// EL USO DE OPTIONAL<USER> INDICA QUE EL RESULTADO PUEDE SER UN USUARIO (SI SE ENCUENTRA) O VACÍO (SI NO SE ENCUENTRA).
	Optional<User> findByEmail(String email);




}