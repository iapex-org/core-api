package com.iapex.services.user;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.iapex.dtos.user.UserMobileAuthenticationDTO;
import com.iapex.dtos.user.UserMobileDTO;
import com.iapex.enums.RoleEnum;
import com.iapex.exceptions.UserAlreadyExistsException;
import com.iapex.models.response.AuthenticationResponse;
import com.iapex.models.response.Response;
import com.iapex.models.token.TokenMobile;
import com.iapex.models.user.UserMobile;
import com.iapex.repositories.token.TokenMobileRepository;
import com.iapex.repositories.user.UserMobileRepository;
import com.iapex.services.email.MobileEmailService;
import com.iapex.services.security.JwtService;

@Service
public class UserMobileService {

    @Autowired
    private final UserMobileRepository userMobileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MobileEmailService mobileEmailService;
    private final TokenMobileRepository tokenMobileRepository;
    private final AuthenticationManager authenticationManager;

    public UserMobileService(UserMobileRepository userMobileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       TokenMobileRepository tokenMobileRepository,
                       AuthenticationManager authenticationManager,
                       MobileEmailService mobileEmailService) {
        this.userMobileRepository = userMobileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenMobileRepository = tokenMobileRepository;
        this.authenticationManager = authenticationManager;
        this.mobileEmailService = mobileEmailService;
    }

 // 1. MÉTODOS PRINCIPALES DE AUTENTICACIÓN

    /**
     * REGISTRA UN NUEVO USUARIO.
     * 
     * ESTE MÉTODO VERIFICA SI YA EXISTE UN USUARIO CON EL CORREO ELECTRÓNICO PROPORCIONADO. 
     * SI NO EXISTE, CREA UN NUEVO USUARIO, LO GUARDA EN EL REPOSITORIO Y ENVÍA UN CORREO 
     * ELECTRÓNICO DE VERIFICACIÓN.
     * 
     * @param request LOS DATOS DEL USUARIO QUE SE DESEA REGISTRAR.
     * @return UNA RESPUESTA INDICANDO QUE EL REGISTRO FUE EXITOSO Y QUE SE DEBE VERIFICAR EL CORREO ELECTRÓNICO.
     * @throws Exception SI YA EXISTE UN USUARIO CON EL CORREO ELECTRÓNICO PROPORCIONADO.
     */
    public Response register(UserMobileDTO request) throws Exception {
        if (userMobileRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Ya existe un usuario registrado con este correo electrónico.");
        }

        UserMobile userMobile = new UserMobile();
        userMobile.setEmail(request.getEmail());
        userMobile.setPassword(passwordEncoder.encode(request.getPassword()));
        userMobile.setPhone(request.getPhone());
        userMobile.setRole(request.getRole() != null ? request.getRole() : RoleEnum.USER_MOBILE);
        userMobile.setStatus(false); // Usuario no verificado inicialmente

        userMobileRepository.save(userMobile);

        return new Response("Su registro fue exitoso. Por favor, verifica tu correo electrónico.");
    }

    /**
     * AUTENTICA A UN USUARIO.
     * 
     * ESTE MÉTODO VERIFICA LAS CREDENCIALES DEL USUARIO Y GENERA UN TOKEN JWT SI LA AUTENTICACIÓN 
     * ES EXITOSA. TAMBIÉN REVISA SI EL USUARIO ESTÁ AUTENTICADO Y SI LA CONTRASEÑA ES CORRECTA.
     * 
     * @param request LOS DATOS DE AUTENTICACIÓN DEL USUARIO.
     * @return UNA RESPUESTA DE AUTENTICACIÓN CON EL TOKEN JWT Y UN MENSAJE DE ÉXITO.
     * @throws RuntimeException SI EL CORREO ELECTRÓNICO O LA CONTRASEÑA SON INCORRECTOS, O SI EL USUARIO NO ESTÁ AUTENTICADO.
     */
    public AuthenticationResponse authenticate(UserMobileAuthenticationDTO request) {
    	UserMobile userMobile;
        if (request.getEmail() != null) {
        	userMobile = userMobileRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Correo electrónico no encontrado. Por favor, verifica que tu correo esté registrado correctamente en la aplicación."));
        } else {
            throw new RuntimeException("Debe proporcionar correo electrónico");
        }
        if (!userMobile.isConfirmed()) {
            throw new RuntimeException("El usuario no está autenticado. Por favor, revise su correo electrónico para confirmar su cuenta.");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new RuntimeException("Debe proporcionar una contraseña");
        }
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                		userMobile.getEmail(),
                    request.getPassword()
                )
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        String token = jwtService.generateToken(userMobile);
        Collection<? extends GrantedAuthority> authorities = userMobile.getAuthorities();
        revokeAllTokenByUser(userMobile);
        saveUserToken(token, userMobile);
        return new AuthenticationResponse(token, "Inicio de sesión exitoso", authorities);
    }

    // 2. MÉTODOS DE GESTIÓN DE CONTRASEÑAS

    public UserMobile findByEmail(String email) {
        return userMobileRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("El correo electrónico no está registrado, asegúrate de estar registrado en la aplicación"));
    }

    
    public boolean verifyCodeAndResetPassword(String verificationCode, String newPassword) {
        // BUSCAR EL EMAIL ASOCIADO CON EL CÓDIGO DE VERIFICACIÓN
        String email = mobileEmailService.getEmailForVerificationCode(verificationCode);
        if (email == null) {
            return false;
        }

        UserMobile userMobile = userMobileRepository.findByEmail(email).orElse(null);
        if (userMobile == null) {
            return false;
        }

        // VERIFICAR EL CÓDIGO
        if (mobileEmailService.verifyCode(verificationCode)) {
        	userMobile.setPassword(passwordEncoder.encode(newPassword));
        	userMobileRepository.save(userMobile);
            return true;
        }
        return false;
    }   

    // 3. MÉTODOS DE GESTIÓN DE USUARIOS

    // OBTIENE TODOS LOS USUARIOS.
    public List<UserMobile> getAllUsers() {
        return userMobileRepository.findAll();
    }

    //OBTIENE UN USUARIO POR SU ID.
    public Optional<UserMobile> getUserById(Long id) {
        return userMobileRepository.findById(id);
    }
    
    //ACTUALIZA UN USUARIO POR SU ID.

    public UserMobile updateUserById(Long id, UserMobileDTO userMobileDTO) {
    	UserMobile userMobile = userMobileRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (userMobileDTO.getEmail() != null && !userMobile.getEmail().equals(userMobileDTO.getEmail())) {
            if (userMobileRepository.findByEmail(userMobileDTO.getEmail()).isPresent()) {
                throw new UserAlreadyExistsException("Ya existe un usuario registrado con este correo electrónico.");
            }
            userMobile.setEmail(userMobileDTO.getEmail());
        }

        if (userMobileDTO.getPassword() != null && !userMobileDTO.getPassword().isEmpty()) userMobile.setPassword(passwordEncoder.encode(userMobileDTO.getPassword()));
        if (userMobileDTO.getPhone() != null && !userMobile.getPhone().equals(userMobileDTO.getPhone())) userMobile.setPhone(userMobileDTO.getPhone());
        if (userMobileDTO.getRole() != null && !userMobile.getRole().equals(userMobileDTO.getRole())) userMobile.setRole(userMobileDTO.getRole());

        return userMobileRepository.save(userMobile);
    }
    
    
    //ELIMINA UN USUARIO POR SU ID.
    public void deleteById(Long id) {
    	userMobileRepository.deleteById(id);
    }


    // 4. MÉTODOS DE AUTORIZACIÓN

    /**
     * VERIFICA SI EL USUARIO AUTENTICADO PUEDE ACCEDER A LOS DATOS DEL USUARIO ESPECIFICADO.
     * 
     * ESTE MÉTODO COMPRUEBA SI EL CORREO ELECTRÓNICO AUTENTICADO COINCIDE CON EL DEL USUARIO 
     * AL QUE SE DESEA ACCEDER O SI EL USUARIO AUTENTICADO TIENE EL ROL DE ADMIN.
     * 
     * @param authenticatedEmail EL CORREO ELECTRÓNICO DEL USUARIO AUTENTICADO.
     * @param userId EL ID DEL USUARIO AL QUE SE DESEA ACCEDER.
     * @return TRUE SI EL USUARIO AUTENTICADO PUEDE ACCEDER, FALSE EN CASO CONTRARIO.
     */
    public boolean canUserAccess(String authenticatedEmail, Long userId) {
    	UserMobile userToAccess = userMobileRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return userToAccess.getEmail().equals(authenticatedEmail) || userHasRole(authenticatedEmail, "ADMIN");
    }

    /**
     * VERIFICA SI EL USUARIO AUTENTICADO PUEDE ACTUALIZAR LOS DATOS DEL USUARIO ESPECIFICADO.
     * 
     * ESTE MÉTODO COMPRUEBA SI EL CORREO ELECTRÓNICO AUTENTICADO COINCIDE CON EL DEL USUARIO 
     * AL QUE SE DESEA ACTUALIZAR O SI EL USUARIO AUTENTICADO TIENE EL ROL DE ADMIN.
     * 
     * @param authenticatedEmail EL CORREO ELECTRÓNICO DEL USUARIO AUTENTICADO.
     * @param userId EL ID DEL USUARIO AL QUE SE DESEA ACTUALIZAR.
     * @return TRUE SI EL USUARIO AUTENTICADO PUEDE ACTUALIZAR, FALSE EN CASO CONTRARIO.
     */
    public boolean canUserUpdate(String authenticatedEmail, Long userId) {
    	UserMobile userToUpdate = userMobileRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return userToUpdate.getEmail().equals(authenticatedEmail) || userHasRole(authenticatedEmail, "ADMIN");
    }

    /**
     * VERIFICA SI EL USUARIO TIENE EL ROL ESPECIFICADO.
     * 
     * ESTE MÉTODO BUSCA AL USUARIO POR SU CORREO ELECTRÓNICO Y COMPRUEBA SI TIENE EL ROL 
     * ESPECIFICADO ENTRE SUS AUTORIDADES.
     * 
     * @param email EL CORREO ELECTRÓNICO DEL USUARIO.
     * @param role EL ROL QUE SE DESEA VERIFICAR.
     * @return TRUE SI EL USUARIO TIENE EL ROL, FALSE EN CASO CONTRARIO.
     */
    private boolean userHasRole(String email, String role) {
    	UserMobile userMobile = userMobileRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return userMobile.getAuthorities().stream()
                   .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    // 5. MÉTODOS AUXILIARES

    /**
     * REVOCA TODOS LOS TOKENS VÁLIDOS DEL USUARIO.
     * 
     * ESTE MÉTODO MARCA TODOS LOS TOKENS VÁLIDOS DEL USUARIO COMO CERRADOS (LOGGED OUT).
     * 
     * @param user EL USUARIO CUYOS TOKENS SE DESEAN REVOCAR.
     */
    private void revokeAllTokenByUser(UserMobile userMobile) {
        List<TokenMobile> validTokens = tokenMobileRepository.findAllTokensByUser(userMobile.getId());
        if (validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(token -> {
            token.setLoggedOut(true);
        });

        tokenMobileRepository.saveAll(validTokens);
    }


    /**
     * GUARDA EL TOKEN DE USUARIO EN LA BASE DE DATOS.
     * 
     * ESTE MÉTODO PRIMERO ELIMINA TODOS LOS TOKENS ANTERIORES DEL USUARIO QUE 
     * ESTÁN MARCADOS COMO CERRADOS (LOGGED OUT). LUEGO, CREA UN NUEVO TOKEN 
     * CON EL JWT PROPORCIONADO, ASIGNA EL USUARIO Y ESTABLECE LA FECHA DE 
     * EXPIRACIÓN A 10 MINUTOS A PARTIR DEL MOMENTO ACTUAL. FINALMENTE, GUARDA 
     * EL NUEVO TOKEN EN EL REPOSITORIO.
     * 
     * @param JWT EL TOKEN JWT QUE SE VA A GUARDAR.
     * @param user EL USUARIO AL QUE PERTENECE EL TOKEN.
     */
    private void saveUserToken(String jwt, UserMobile userMobile) {
        List<TokenMobile> loggedOutTokens = tokenMobileRepository.findAllByUserMobileAndLoggedOut(userMobile, true);
        tokenMobileRepository.deleteAll(loggedOutTokens);

        TokenMobile token = new TokenMobile();
        token.setToken(jwt);
        token.setUserMobile(userMobile);
        token.setExpirationDate(calculateExpireDate());
        token.setLoggedOut(false);

        tokenMobileRepository.save(token);
    }

    /**
     * CALCULA LA FECHA DE EXPIRACIÓN DEL TOKEN.
     * 
     * ESTE MÉTODO ESTABLECE LA FECHA DE EXPIRACIÓN DEL TOKEN A 1 MINUTOS 
     * A PARTIR DEL MOMENTO EN QUE ES GENERADO.
     * 
     * @return LA FECHA DE EXPIRACIÓN DEL TOKEN.
     */
    public Date calculateExpireDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 10);
        return calendar.getTime();
    }

}




