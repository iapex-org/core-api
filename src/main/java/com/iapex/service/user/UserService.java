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
import com.iapex.dto.user.UserDTO;
import com.iapex.exceptions.UserAlreadyExistsException;
import com.iapex.model.*;
import com.iapex.model.response.AuthenticationResponse;
import com.iapex.model.response.Response;
import com.iapex.model.token.Token;
import com.iapex.model.user.Role;
import com.iapex.model.user.User;
import com.iapex.repository.token.TokenRepository;
import com.iapex.repository.user.UserRepository;
import com.iapex.service.email.EmailService;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       TokenRepository tokenRepository,
                       AuthenticationManager authenticationManager,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
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
    public Response register(UserDTO request) throws Exception {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Ya existe un usuario registrado con este correo electrónico.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);
        user.setStatus(false); // Usuario no verificado inicialmente

        userRepository.save(user);

        String verificationCode = emailService.sendVerificationEmail(user);

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
        User user;
        if (request.getEmail() != null) {
            user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Correo electrónico no encontrado. Por favor, verifica que tu correo esté registrado correctamente en la aplicación."));
        } else {
            throw new RuntimeException("Debe proporcionar correo electrónico");
        }
        if (!user.isConfirmed()) {
            throw new RuntimeException("El usuario no está confirmado. Por favor, revise su correo electrónico para confirmar su cuenta.");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new RuntimeException("Debe proporcionar una contraseña");
        }
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    request.getPassword()
                )
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        String token = jwtService.generateToken(user);
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        revokeAllTokenByUser(user);
        saveUserToken(token, user);
        return new AuthenticationResponse(token, "Inicio de sesión exitoso", authorities);
    }

    // 2. MÉTODOS DE GESTIÓN DE CONTRASEÑAS

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("El correo electrónico no está registrado, asegúrate de estar registrado en la aplicación"));
    }

    
    public boolean verifyCodeAndResetPassword(String verificationCode, String newPassword) {
        // BUSCAR EL EMAIL ASOCIADO CON EL CÓDIGO DE VERIFICACIÓN
        String email = emailService.getEmailForVerificationCode(verificationCode);
        if (email == null) {
            return false;
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return false;
        }

        // VERIFICAR EL CÓDIGO
        if (emailService.verifyCode(verificationCode)) {
        	user.setPassword(passwordEncoder.encode(newPassword));
        	userRepository.save(user);
            return true;
        }
        return false;
    }   

    // 3. MÉTODOS DE GESTIÓN DE USUARIOS

    // OBTIENE TODOS LOS USUARIOS.
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    //OBTIENE UN USUARIO POR SU ID.
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    //ACTUALIZA UN USUARIO POR SU ID.

    public User updateUserById(Long id, UserDTO userDto) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (userDto.getEmail() != null && !user.getEmail().equals(userDto.getEmail())) {
            if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
                throw new UserAlreadyExistsException("Ya existe un usuario registrado con este correo electrónico.");
            }
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        if (userDto.getPhone() != null && !user.getPhone().equals(userDto.getPhone())) user.setPhone(userDto.getPhone());
        if (userDto.getRole() != null && !user.getRole().equals(userDto.getRole())) user.setRole(userDto.getRole());

        return userRepository.save(user);
    }
    
    
    //ELIMINA UN USUARIO POR SU ID.
    public void deleteById(Long id) {
        userRepository.deleteById(id);
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
        User userToAccess = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
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
        User userToUpdate = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
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
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user.getAuthorities().stream()
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
    private void revokeAllTokenByUser(User user) {
        List<Token> validTokens = tokenRepository.findAllTokensByUser(user.getIdUser());
        if (validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(token -> {
            token.setLoggedOut(true);
        });

        tokenRepository.saveAll(validTokens);
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
    private void saveUserToken(String jwt, User user) {
        List<Token> loggedOutTokens = tokenRepository.findAllByUserAndLoggedOut(user, true);
        tokenRepository.deleteAll(loggedOutTokens);

        Token token = new Token();
        token.setToken(jwt);
        token.setUser(user);
        token.setExpirationDate(calculateExpireDate());
        token.setLoggedOut(false);

        tokenRepository.save(token);
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




