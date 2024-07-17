package com.iapex.repository.token;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import com.iapex.model.token.TokenWeb;
import com.iapex.model.user.UserWeb;

public interface TokenWebRepository extends JpaRepository<TokenWeb, Long> {

    /**
     * Encuentra todos los tokens que han expirado antes de la fecha actual.
     * 
     * @param currentDate La fecha actual
     * @return Una lista de tokens expirados
     */
    @Query("select t from TokenWeb t where t.expirationDate <= :currentDate")
    List<TokenWeb> findExpiredTokens(@Param("currentDate") Date currentDate);

    /**
     * Elimina todos los tokens que han expirado antes de la fecha actual.
     * 
     * @param currentDate La fecha actual
     */
    @Transactional
    @Modifying
    @Query("delete from TokenWeb t where t.expirationDate <= :currentDate")
    void deleteExpiredTokens(@Param("currentDate") Date currentDate);

    /**
     * Encuentra todos los tokens activos (no cerrados) de un usuario específico.
     * 
     * @param id El ID del usuario institución
     * @return Una lista de tokens activos del usuario
     */
    @Query("select t from TokenWeb t inner join UserWeb u on t.userWeb.id = u.id where u.id = :id and t.loggedOut = false")
    List<TokenWeb> findAllTokensByUser(@Param("id") Long id);

    /**
     * Busca un token por su valor.
     * 
     * @param token El valor del token
     * @return Un Optional que contiene el token si se encuentra
     */
    Optional<TokenWeb> findByToken(String token);

    /**
     * Encuentra todos los tokens de un usuario institución específico, 
     * filtrados por su estado de cierre de sesión.
     * 
     * @param userWeb El usuario institución
     * @param loggedOut El estado de cierre de sesión
     * @return Una lista de tokens que coinciden con los criterios
     */
    List<TokenWeb> findAllByUserWebAndLoggedOut(UserWeb userWeb, boolean loggedOut);

    /**
     * Encuentra todos los tokens asociados a un usuario institución específico.
     * 
     * @param id El ID del usuario institución
     * @return Una lista de todos los tokens del usuario institución
     */
    List<TokenWeb> findByUserWeb_id(Long id);
}