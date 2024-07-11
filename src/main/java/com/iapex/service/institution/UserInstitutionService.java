package com.iapex.service.institution;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.iapex.exceptions.UserAlreadyExistsException;
import com.iapex.institution.DTO.UserInstitutionAuthDTO;
import com.iapex.institution.DTO.UserInstitutionDTO;
import com.iapex.model.AuthenticationResponse;
import com.iapex.model.Response;
import com.iapex.model.Role;
import com.iapex.model.institution.Institution;
import com.iapex.model.institution.TokenInstitution;
import com.iapex.model.institution.UserInstitution;
import com.iapex.repository.InstitutionRepository;
import com.iapex.repository.TokenInstitutionRepository;
import com.iapex.repository.UserInstitutionRepository;
import com.iapex.service.JwtService;
import com.iapex.service.mail.InstitutionEmailService;

@Service
public class UserInstitutionService {

    @Autowired
    private UserInstitutionRepository userInstitutionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private InstitutionEmailService institutionEmailService;

    @Autowired
    private TokenInstitutionRepository tokenInstitutionRepository;
    
    @Autowired
    private InstitutionRepository institutionRepository;
    
    @Autowired
    private AuthenticationManager authenticationManager;

    public UserInstitutionService(
            UserInstitutionRepository userInstitutionRepository,
            InstitutionRepository institutionRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TokenInstitutionRepository tokenInstitutionRepository,
            AuthenticationManager authenticationManager,
            InstitutionEmailService institutionEmailService) {
        this.userInstitutionRepository = userInstitutionRepository;
        this.institutionRepository = institutionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenInstitutionRepository = tokenInstitutionRepository;
        this.authenticationManager = authenticationManager;
        this.institutionEmailService = institutionEmailService;
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
    public Response registerUser(UserInstitutionDTO request) throws Exception {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Por favor ingresa un correo electrónico.");
        }
    	
    	if (userInstitutionRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Ya existe un usuario registrado con este correo electrónico.");
        }

        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setName(request.getName());
        userInstitution.setEmail(request.getEmail());
        userInstitution.setFathername(request.getFathername());
        userInstitution.setMothername(request.getMothername());
        userInstitution.setPassword(passwordEncoder.encode(request.getPassword()));
        userInstitution.setCharge(request.getCharge());
        userInstitution.setRole(request.getRole() != null ? request.getRole() : Role.USER);
        userInstitution.setStatus(false);

        // Buscar la institución por su nombre
        Institution institution = institutionRepository.findByName(request.getInstitutionName())
                .orElseThrow(() -> new Exception("Institución no encontrada"));
        userInstitution.setInstitution(institution);

        userInstitutionRepository.save(userInstitution);

        String verificationCode = institutionEmailService.sendVerificationUserInstitutionEmail(userInstitution);

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
    public AuthenticationResponse authenticateInstitution(UserInstitutionAuthDTO request) {
    	UserInstitution userInstitution;
        if (request.getEmail() != null) {
        	userInstitution = userInstitutionRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Correo electrónico no encontrado. Por favor, verifica que tu correo esté registrado correctamente en la aplicación."));
        } else {
            throw new RuntimeException("Debe proporcionar correo electrónico");
        }
        if (!userInstitution.isConfirmed()) {
            throw new RuntimeException("El usuario no está confirmado. Por favor, revise su correo electrónico para confirmar su cuenta.");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new RuntimeException("Debe proporcionar una contraseña");
        }
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                		userInstitution.getEmail(),
                    request.getPassword()
                )
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        String token = jwtService.generateTokenUserInstitution(userInstitution);
        Collection<? extends GrantedAuthority> authorities = userInstitution.getAuthorities();
        revokeAllTokenByUserInstitution(userInstitution);
        saveUserTokenInstitution(token, userInstitution);
        return new AuthenticationResponse(token, "Inicio de sesión exitoso", authorities);
    }
    
    
    

    // 5. MÉTODOS AUXILIARES

    /**
     * REVOCA TODOS LOS TOKENS VÁLIDOS DEL USUARIO.
     *
     * ESTE MÉTODO MARCA TODOS LOS TOKENS VÁLIDOS DEL USUARIO COMO CERRADOS (LOGGED OUT).
     *
     * @param userInstitution EL USUARIO CUYOS TOKENS SE DESEAN REVOCAR.
     */
    private void revokeAllTokenByUserInstitution(UserInstitution userInstitution) {
        List<TokenInstitution> validTokens = tokenInstitutionRepository.findAllTokensByUser(userInstitution.getIdUserInstitution());
        if (validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(token -> {
            token.setLoggedOut(true);
        });

        tokenInstitutionRepository.saveAll(validTokens);
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
     * @param jwt EL TOKEN JWT QUE SE VA A GUARDAR.
     * @param userInstitution EL USUARIO AL QUE PERTENECE EL TOKEN.
     */
    private void saveUserTokenInstitution(String jwt, UserInstitution userInstitution) {
        List<TokenInstitution> loggedOutTokens = tokenInstitutionRepository.findAllByUserInstitutionAndLoggedOut(userInstitution, true);
        tokenInstitutionRepository.deleteAll(loggedOutTokens);

        TokenInstitution tokenInstitution = new TokenInstitution();
        tokenInstitution.setToken(jwt);
        tokenInstitution.setUserInstitution(userInstitution);
        tokenInstitution.setExpirationDate(calcularFechaExpiracion());
        tokenInstitution.setLoggedOut(false);

        tokenInstitutionRepository.save(tokenInstitution);
    }
    
    /**
     * CALCULA LA FECHA DE EXPIRACIÓN DEL TOKEN.
     * 
     * ESTE MÉTODO ESTABLECE LA FECHA DE EXPIRACIÓN DEL TOKEN A 10 MINUTOS 
     * A PARTIR DEL MOMENTO EN QUE ES GENERADO.
     * 
     * @return LA FECHA DE EXPIRACIÓN DEL TOKEN.
     */
    public Date calcularFechaExpiracion() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 1);
        return calendar.getTime();
    }
    
    /**
     * ENCUENTRA UN USUARIO INSTITUCIONAL POR CORREO ELECTRÓNICO.
     *
     * @PARAM EMAIL EL CORREO ELECTRÓNICO A BUSCAR.
     * @RETURN EL USUARIO INSTITUCIONAL SI SE ENCUENTRA.
     * @THROWS RUNTIMEEXCEPTION SI EL USUARIO NO SE ENCUENTRA.
     */
    public UserInstitution findByEmail(String email) {
        return userInstitutionRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
    
    public boolean verifyCodeAndResetPassword(String verificationCode, String newPassword) {
        // BUSCAR EL EMAIL ASOCIADO CON EL CÓDIGO DE VERIFICACIÓN
        String email = institutionEmailService.getEmailForVerificationCode(verificationCode);
        if (email == null) {
            return false;
        }

        UserInstitution userInstitution = userInstitutionRepository.findByEmail(email).orElse(null);
        if (userInstitution == null) {
            return false;
        }

        // VERIFICAR EL CÓDIGO
        if (institutionEmailService.verifyCode(verificationCode)) {
            userInstitution.setPassword(passwordEncoder.encode(newPassword));
            userInstitutionRepository.save(userInstitution);
            return true;
        }
        return false;
    }    
}