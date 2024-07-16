package com.iapex.service.user;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.iapex.dto.user.UserAuthenticationDTO;
import com.iapex.dto.user.UserMovilDTO;
import com.iapex.exceptions.UserAlreadyExistsException;
import com.iapex.model.response.AuthenticationResponse;
import com.iapex.model.response.Response;
import com.iapex.model.token.Token;
import com.iapex.model.user.Role;
import com.iapex.model.user.UserMovil;
import com.iapex.repository.token.TokenMovilRepository;
import com.iapex.repository.user.UserMovilRepository;
import com.iapex.service.email.MovilEmailService;

@Service
public class UserMovilService {

    @Autowired
    private final UserMovilRepository userMovilRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MovilEmailService movilEmailService;
    private final TokenMovilRepository tokenMovilRepository;
    private final AuthenticationManager authenticationManager;

    public UserMovilService(UserMovilRepository userMovilRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       TokenMovilRepository tokenMovilRepository,
                       AuthenticationManager authenticationManager,
                       MovilEmailService movilEmailService) {
        this.userMovilRepository = userMovilRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenMovilRepository = tokenMovilRepository;
        this.authenticationManager = authenticationManager;
        this.movilEmailService = movilEmailService;
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
    public Response register(UserMovilDTO request) throws Exception {
        if (userMovilRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Ya existe un usuario registrado con este correo electrónico.");
        }

        UserMovil userMovil = new UserMovil();
        userMovil.setEmail(request.getEmail());
        userMovil.setPassword(passwordEncoder.encode(request.getPassword()));
        userMovil.setPhone(request.getPhone());
        userMovil.setRole(request.getRole() != null ? request.getRole() : Role.USER);
        userMovil.setStatus(false); // Usuario no verificado inicialmente

        userMovilRepository.save(userMovil);

        String verificationCode = movilEmailService.sendVerificationEmail(userMovil);

        return new Response("Su registro fue exitoso. Por favor, verifica tu correo electrónico.");
    }

    /**
     * AUTENTICA A UN USUARIO.
     * 
     * ESTE MÉTODO VERIFICA LAS CREDENCIALES DEL USUARIO Y GENERA UN TOKEN JWT SI LA AUTENTICACIÓN 
     * ES EXITOSA. TAMBIÉN REVISA SI EL USUARIO ESTÁ CONFIRMADO Y SI LA CONTRASEÑA ES CORRECTA.
     * 
     * @param request LOS DATOS DE AUTENTICACIÓN DEL USUARIO.
     * @return UNA RESPUESTA DE AUTENTICACIÓN CON EL TOKEN JWT Y UN MENSAJE DE ÉXITO.
     * @throws RuntimeException SI EL CORREO ELECTRÓNICO O LA CONTRASEÑA SON INCORRECTOS, O SI EL USUARIO NO ESTÁ CONFIRMADO.
     */
    public AuthenticationResponse authenticate(UserAuthenticationDTO request) {
    	UserMovil userMovil;
        if (request.getEmail() != null) {
        	userMovil = userMovilRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Correo electrónico no encontrado. Por favor, verifica que tu correo esté registrado correctamente en la aplicación."));
        } else {
            throw new RuntimeException("Debe proporcionar correo electrónico");
        }
        if (!userMovil.isConfirmed()) {
            throw new RuntimeException("El usuario no está confirmado. Por favor, revise su correo electrónico para confirmar su cuenta.");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new RuntimeException("Debe proporcionar una contraseña");
        }
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                		userMovil.getEmail(),
                    request.getPassword()
                )
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        String token = jwtService.generateToken(userMovil);
        Collection<? extends GrantedAuthority> authorities = userMovil.getAuthorities();
        revokeAllTokenByUser(userMovil);
        saveUserToken(token, userMovil);
        return new AuthenticationResponse(token, "Inicio de sesión exitoso", authorities);
    }

    // 2. MÉTODOS DE GESTIÓN DE CONTRASEÑAS

    public UserMovil findByEmail(String email) {
        return userMovilRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("El correo electrónico no está registrado, asegúrate de estar registrado en la aplicación"));
    }

    
    public boolean verifyCodeAndResetPassword(String verificationCode, String newPassword) {
        // BUSCAR EL EMAIL ASOCIADO CON EL CÓDIGO DE VERIFICACIÓN
        String email = movilEmailService.getEmailForVerificationCode(verificationCode);
        if (email == null) {
            return false;
        }

        UserMovil userMovil = userMovilRepository.findByEmail(email).orElse(null);
        if (userMovil == null) {
            return false;
        }

        // VERIFICAR EL CÓDIGO
        if (movilEmailService.verifyCode(verificationCode)) {
        	userMovil.setPassword(passwordEncoder.encode(newPassword));
        	userMovilRepository.save(userMovil);
            return true;
        }
        return false;
    }   

    // 3. MÉTODOS DE GESTIÓN DE USUARIOS

    // OBTIENE TODOS LOS USUARIOS.
    public List<UserMovil> getAllUsers() {
        return userMovilRepository.findAll();
    }

    //OBTIENE UN USUARIO POR SU ID.
    public Optional<UserMovil> getUserById(Long id) {
        return userMovilRepository.findById(id);
    }
    
    //ACTUALIZA UN USUARIO POR SU ID.

    public UserMovil updateUserById(Long id, UserMovilDTO userMovilDTO) {
    	UserMovil userMovil = userMovilRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (userMovilDTO.getEmail() != null && !userMovil.getEmail().equals(userMovilDTO.getEmail())) {
            if (userMovilRepository.findByEmail(userMovilDTO.getEmail()).isPresent()) {
                throw new UserAlreadyExistsException("Ya existe un usuario registrado con este correo electrónico.");
            }
            userMovil.setEmail(userMovilDTO.getEmail());
        }

        if (userMovilDTO.getPassword() != null && !userMovilDTO.getPassword().isEmpty()) userMovil.setPassword(passwordEncoder.encode(userMovilDTO.getPassword()));
        if (userMovilDTO.getPhone() != null && !userMovil.getPhone().equals(userMovilDTO.getPhone())) userMovil.setPhone(userMovilDTO.getPhone());
        if (userMovilDTO.getRole() != null && !userMovil.getRole().equals(userMovilDTO.getRole())) userMovil.setRole(userMovilDTO.getRole());

        return userMovilRepository.save(userMovil);
    }
    
    
    //ELIMINA UN USUARIO POR SU ID.
    public void deleteById(Long id) {
    	userMovilRepository.deleteById(id);
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
    	UserMovil userToAccess = userMovilRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
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
    	UserMovil userToUpdate = userMovilRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
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
    	UserMovil userMovil = userMovilRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return userMovil.getAuthorities().stream()
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
    private void revokeAllTokenByUser(UserMovil userMovil) {
        List<Token> validTokens = tokenMovilRepository.findAllTokensByUser(userMovil.getIdUser());
        if (validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(token -> {
            token.setLoggedOut(true);
        });

        tokenMovilRepository.saveAll(validTokens);
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
    private void saveUserToken(String jwt, UserMovil userMovil) {
        List<Token> loggedOutTokens = tokenMovilRepository.findAllByUserMovilAndLoggedOut(userMovil, true);
        tokenMovilRepository.deleteAll(loggedOutTokens);

        Token token = new Token();
        token.setToken(jwt);
        token.setUserMovil(userMovil);
        token.setExpirationDate(calculateExpireDate());
        token.setLoggedOut(false);

        tokenMovilRepository.save(token);
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




