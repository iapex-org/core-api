package com.iapex.repository.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.iapex.model.token.Token;
import com.iapex.model.user.User;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    // Consulta para encontrar tokens expirados antes de la fecha actual
    @Query("select t from Token t where t.expirationDate <= :currentDate")
    List<Token> findExpiredTokens(@Param("currentDate") Date currentDate);

    // Consulta para eliminar tokens expirados antes de la fecha actual
    @Transactional
    @Modifying
    @Query("delete from Token t where t.expirationDate <= :currentDate")
    void deleteExpiredTokens(@Param("currentDate") Date currentDate);

    // Consulta para encontrar todos los tokens de un usuario que no han sido marcados como 'loggedOut'
    @Query("select t from Token t inner join User u on t.user.idUser = u.idUser where u.idUser = :userId and t.loggedOut = false")
    List<Token> findAllTokensByUser(@Param("userId") Long userId);

    // Consulta para encontrar un token por su valor
    Optional<Token> findByToken(String token);

    // Consulta para encontrar todos los tokens de un usuario que han sido marcados como 'loggedOut' o no
    List<Token> findAllByUserAndLoggedOut(User user, boolean loggedOut);

    // Consulta para encontrar todos los tokens de un usuario por su ID
    List<Token> findByUser_IdUser(Long idUser);
}