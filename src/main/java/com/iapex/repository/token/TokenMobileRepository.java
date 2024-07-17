package com.iapex.repository.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.iapex.model.token.TokenMobile;
import com.iapex.model.user.UserMobile;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TokenMobileRepository extends JpaRepository<TokenMobile, Long> {

    // Consulta para encontrar tokens expirados antes de la fecha actual
    @Query("select t from TokenMobile t where t.expirationDate <= :currentDate")
    List<TokenMobile> findExpiredTokens(@Param("currentDate") Date currentDate);

    // Consulta para eliminar tokens expirados antes de la fecha actual
    @Transactional
    @Modifying
    @Query("delete from TokenMobile t where t.expirationDate <= :currentDate")
    void deleteExpiredTokens(@Param("currentDate") Date currentDate);

    // Consulta para encontrar todos los tokens de un usuario que no han sido marcados como 'loggedOut'
    @Query("select t from TokenMobile t inner join UserMobile u on t.userMobile.id = u.id where u.id = :id and t.loggedOut = false")
    List<TokenMobile> findAllTokensByUser(@Param("id") Long id);

    // Consulta para encontrar un token por su valor
    Optional<TokenMobile> findByToken(String token);

    // Consulta para encontrar todos los tokens de un usuario que han sido marcados como 'loggedOut' o no
    List<TokenMobile> findAllByUserMobileAndLoggedOut(UserMobile userMobile, boolean loggedOut);

    // Consulta para encontrar todos los tokens de un usuario por su ID
    List<TokenMobile> findByUserMobile_id(Long id);
}