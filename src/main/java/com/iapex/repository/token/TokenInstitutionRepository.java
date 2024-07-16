package com.iapex.repository.token;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.iapex.model.token.TokenInstitution;
import com.iapex.model.user.UserInstitution;

public interface TokenInstitutionRepository extends JpaRepository<TokenInstitution, Long> {

    /**
     * Encuentra todos los tokens que han expirado antes de la fecha actual.
     * 
     * @param currentDate La fecha actual
     * @return Una lista de tokens expirados
     */
    @Query("select t from TokenInstitution t where t.expirationDate <= :currentDate")
    List<TokenInstitution> findExpiredTokens(@Param("currentDate") Date currentDate);

    /**
     * Elimina todos los tokens que han expirado antes de la fecha actual.
     * 
     * @param currentDate La fecha actual
     */
    @Transactional
    @Modifying
    @Query("delete from TokenInstitution t where t.expirationDate <= :currentDate")
    void deleteExpiredTokens(@Param("currentDate") Date currentDate);

    /**
     * Encuentra todos los tokens activos (no cerrados) de un usuario específico.
     * 
     * @param userId El ID del usuario institución
     * @return Una lista de tokens activos del usuario
     */
    @Query("select t from TokenInstitution t inner join UserInstitution u on t.userInstitution.idUserInstitution = u.idUserInstitution where u.idUserInstitution = :userId and t.loggedOut = false")
    List<TokenInstitution> findAllTokensByUser(@Param("userId") Long userId);

    /**
     * Busca un token por su valor.
     * 
     * @param token El valor del token
     * @return Un Optional que contiene el token si se encuentra
     */
    Optional<TokenInstitution> findByToken(String token);

    /**
     * Encuentra todos los tokens de un usuario institución específico, 
     * filtrados por su estado de cierre de sesión.
     * 
     * @param userInstitution El usuario institución
     * @param loggedOut El estado de cierre de sesión
     * @return Una lista de tokens que coinciden con los criterios
     */
    List<TokenInstitution> findAllByUserInstitutionAndLoggedOut(UserInstitution userInstitution, boolean loggedOut);

    /**
     * Encuentra todos los tokens asociados a un usuario institución específico.
     * 
     * @param idUserInstitution El ID del usuario institución
     * @return Una lista de todos los tokens del usuario institución
     */
    List<TokenInstitution> findByUserInstitution_IdUserInstitution(Long idUserInstitution);
}