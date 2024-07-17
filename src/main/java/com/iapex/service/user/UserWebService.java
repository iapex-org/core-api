package com.iapex.service.user;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.iapex.dto.user.UserWebAuthenticationDTO;
import com.iapex.dto.user.UserWebDTO;
import com.iapex.exceptions.InstitutionNotFoundException;
import com.iapex.exceptions.UserAlreadyExistsException;
import com.iapex.model.institution.Institution;
import com.iapex.model.response.AuthenticationResponse;
import com.iapex.model.response.Response;
import com.iapex.model.role.Role;
import com.iapex.model.token.TokenWeb;
import com.iapex.model.user.UserWeb;
import com.iapex.repository.institution.InstitutionRepository;
import com.iapex.repository.token.TokenInstitutionRepository;
import com.iapex.repository.user.UserWebRepository;
import com.iapex.service.email.WebEmailService;
import com.iapex.service.security.JwtService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class UserWebService {

    @Autowired
    private UserWebRepository userWebRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private WebEmailService webEmailService;

    @Autowired
    private TokenInstitutionRepository tokenInstitutionRepository;
    
    @Autowired
    private InstitutionRepository institutionRepository;
    
    @Autowired
    private AuthenticationManager authenticationManager;

    public UserWebService(
            UserWebRepository userWebRepository,
            InstitutionRepository institutionRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TokenInstitutionRepository tokenInstitutionRepository,
            AuthenticationManager authenticationManager,
            WebEmailService webEmailService) {
        this.userWebRepository = userWebRepository;
        this.institutionRepository = institutionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenInstitutionRepository = tokenInstitutionRepository;
        this.authenticationManager = authenticationManager;
        this.webEmailService = webEmailService;
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
    public Response registerUser(UserWebDTO request) throws Exception {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Por favor ingresa un correo electrónico.");
        }
    	
    	if (userWebRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Ya existe un usuario registrado con este correo electrónico.");
        }

        UserWeb userInstitution = new UserWeb();
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
        	    .orElseThrow(() -> new InstitutionNotFoundException("Institución no encontrada"));
        	userInstitution.setInstitution(institution);

        	userWebRepository.save(userInstitution);

        String verificationCode = webEmailService.sendVerificationUserInstitutionEmail(userInstitution);

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
    public AuthenticationResponse authenticateInstitution(UserWebAuthenticationDTO request) {
    	UserWeb userInstitution;
        if (request.getEmail() != null) {
        	userInstitution = userWebRepository.findByEmail(request.getEmail())
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
    
    /**
     * OBTIENE TODOS LOS USUARIOS INSTITUCIONALES.
     *
     * ESTE MÉTODO RECUPERA TODOS LOS USUARIOS INSTITUCIONALES ALMACENADOS EN LA BASE DE DATOS.
     *
     * @return UNA LISTA DE TODOS LOS USUARIOS INSTITUCIONALES.
     */
    public List<UserWebDTO> getAllUserDTOs() {
        List<UserWeb> users = userWebRepository.findAll();
        return users.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
    }

    
    //OBTIENE TODOS LOS USUARIOS DE LA INSTITUCIÓN DEL USUARIO AUTENTICADO.
    public List<UserWebDTO> getUsersFromSameInstitution(String authenticatedEmail) {
        UserWeb authenticatedUser = userWebRepository.findByEmail(authenticatedEmail)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        List<UserWeb> users = userWebRepository.findByInstitution(authenticatedUser.getInstitution());
        
        return users.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
    }

    private UserWebDTO convertToDTO(UserWeb userInstitution) {
        UserWebDTO dto = new UserWebDTO();
        dto.setIdUserInstitution(userInstitution.getIdUserInstitution());
        dto.setName(userInstitution.getName());
        dto.setFathername(userInstitution.getFathername());
        dto.setMothername(userInstitution.getMothername());
        dto.setEmail(userInstitution.getEmail());
        dto.setPassword(userInstitution.getPassword()); // Nota: normalmente no se devuelve la contraseña
        dto.setCharge(userInstitution.getCharge());
        dto.setInstitutionName(userInstitution.getInstitution().getName());
        dto.setRole(userInstitution.getRole());
        dto.setStatus(userInstitution.isStatus());
        return dto;
    }
    

    // 5. MÉTODOS AUXILIARES

    /**
     * REVOCA TODOS LOS TOKENS VÁLIDOS DEL USUARIO.
     *
     * ESTE MÉTODO MARCA TODOS LOS TOKENS VÁLIDOS DEL USUARIO COMO CERRADOS (LOGGED OUT).
     *
     * @param userInstitution EL USUARIO CUYOS TOKENS SE DESEAN REVOCAR.
     */
    private void revokeAllTokenByUserInstitution(UserWeb userInstitution) {
        List<TokenWeb> validTokens = tokenInstitutionRepository.findAllTokensByUser(userInstitution.getIdUserInstitution());
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
    private void saveUserTokenInstitution(String jwt, UserWeb userInstitution) {
        List<TokenWeb> loggedOutTokens = tokenInstitutionRepository.findAllByUserInstitutionAndLoggedOut(userInstitution, true);
        tokenInstitutionRepository.deleteAll(loggedOutTokens);

        TokenWeb tokenInstitution = new TokenWeb();
        tokenInstitution.setToken(jwt);
        tokenInstitution.setUserInstitution(userInstitution);
        tokenInstitution.setExpirationDate(calculateExpireDate());
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
    public Date calculateExpireDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 10);
        return calendar.getTime();
    }
    
    /**
     * ENCUENTRA UN USUARIO INSTITUCIONAL POR CORREO ELECTRÓNICO.
     *
     * @PARAM EMAIL EL CORREO ELECTRÓNICO A BUSCAR.
     * @RETURN EL USUARIO INSTITUCIONAL SI SE ENCUENTRA.
     * @THROWS RUNTIMEEXCEPTION SI EL USUARIO NO SE ENCUENTRA.
     */
    public UserWeb findByEmail(String email) {
        return userWebRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("El correo no está registrado en la aplicación"));
    }
    
    public boolean verifyCodeAndResetPassword(String verificationCode, String newPassword) {
        // BUSCAR EL EMAIL ASOCIADO CON EL CÓDIGO DE VERIFICACIÓN
        String email = webEmailService.getEmailForVerificationCode(verificationCode);
        if (email == null) {
            return false;
        }

        UserWeb userInstitution = userWebRepository.findByEmail(email).orElse(null);
        if (userInstitution == null) {
            return false;
        }

        // VERIFICAR EL CÓDIGO
        if (webEmailService.verifyCode(verificationCode)) {
            userInstitution.setPassword(passwordEncoder.encode(newPassword));
            userWebRepository.save(userInstitution);
            return true;
        }
        return false;
    }    
     
    //OBTENER POR ID PARA DTO TRANSFER
    public UserWebDTO getUserInstitutionDTOById(Long id) throws Exception {
        UserWeb userInstitution = getUserInstitutionById(id);
        return convertToDTO(userInstitution);
    }
    
 // ELIMINA UN USUARIO POR SU ID.
    @Transactional
    public void deleteById(Long id) {
        // BUSCA EL USUARIO INSTITUCIÓN POR SU ID O LANZA UNA EXCEPCIÓN SI NO SE ENCUENTRA
        UserWeb userInstitution = userWebRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
        // BUSCA Y ELIMINA TODOS LOS TOKENS ASOCIADOS AL USUARIO INSTITUCIÓN
        List<TokenWeb> tokens = tokenInstitutionRepository.findByUserInstitution_IdUserInstitution(id);
        tokenInstitutionRepository.deleteAll(tokens);
        // ELIMINA LA REFERENCIA A LA INSTITUCIÓN PARA EVITAR LA VIOLACIÓN DE CLAVE FORÁNEA
        userInstitution.setInstitution(null);
        // GUARDA EL USUARIO INSTITUCIÓN ACTUALIZADO PARA APLICAR EL CAMBIO
        userWebRepository.save(userInstitution);
        // ELIMINA FÍSICAMENTE EL USUARIO INSTITUCIÓN DE LA BASE DE DATOS
        userWebRepository.delete(userInstitution);
    }


    
    //OBTENER POR ID
    public UserWeb getUserInstitutionById(Long id) throws Exception {
        return userWebRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));
    }
    
    //ACTUALIZAR UN USUARIO POR SU ID.
    public Response updateUserInstitution(Long id, UserWebDTO request) throws Exception {
        UserWeb userInstitution = getUserInstitutionById(id);
        boolean emailChanged = false; // Verificar si el email está cambiando
        if (!Objects.equals(userInstitution.getEmail(), request.getEmail())) { 
        if (userWebRepository.findByEmail(request.getEmail()).isPresent()) { throw new UserAlreadyExistsException("Ya existe un usuario registrado con este correo electrónico.");
        } emailChanged = true; }

        // Actualizar campos
        if (!Objects.equals(userInstitution.getName(), request.getName())) userInstitution.setName(request.getName());
        if (emailChanged) userInstitution.setEmail(request.getEmail());
        if (!Objects.equals(userInstitution.getFathername(), request.getFathername())) userInstitution.setFathername(request.getFathername());
        if (!Objects.equals(userInstitution.getMothername(), request.getMothername())) userInstitution.setMothername(request.getMothername());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) userInstitution.setPassword(passwordEncoder.encode(request.getPassword()));
        if (!Objects.equals(userInstitution.getCharge(), request.getCharge())) userInstitution.setCharge(request.getCharge());
        if (request.getRole() != null) {
        if (!Objects.equals(userInstitution.getRole(), request.getRole())) userInstitution.setRole(request.getRole()); } else {request.setRole(userInstitution.getRole()); }        
       
        if (!Objects.equals(userInstitution.getInstitution().getName(), request.getInstitutionName())) {
            Institution institution = institutionRepository.findByName(request.getInstitutionName())
                .orElseThrow(() -> new InstitutionNotFoundException("Institución no encontrada: " + request.getInstitutionName()));
            userInstitution.setInstitution(institution);
        }
        if (emailChanged) {userInstitution.setStatus(false); }
        userWebRepository.save(userInstitution);
        
        if (emailChanged) {
            String verificationCode = webEmailService.sendVerificationUserInstitutionEmail(userInstitution);
            return new Response("El usuario ha sido actualizado exitosamente. Se ha enviado un correo de verificación al nuevo email.");
        }
        return new Response("El usuario ha sido actualizado exitosamente.");
    }
}

