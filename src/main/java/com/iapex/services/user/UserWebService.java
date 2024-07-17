package com.iapex.services.user;

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

import com.iapex.dtos.user.UserWebAuthenticationDTO;
import com.iapex.dtos.user.UserWebDTO;
import com.iapex.enums.RoleEnum;
import com.iapex.exceptions.InstitutionNotFoundException;
import com.iapex.exceptions.UserAlreadyExistsException;
import com.iapex.models.institution.Institution;
import com.iapex.models.response.AuthenticationResponse;
import com.iapex.models.response.Response;
import com.iapex.models.token.TokenWeb;
import com.iapex.models.user.UserWeb;
import com.iapex.repositories.institution.InstitutionRepository;
import com.iapex.repositories.token.TokenWebRepository;
import com.iapex.repositories.user.UserWebRepository;
import com.iapex.services.email.WebEmailService;
import com.iapex.services.security.JwtService;

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
    private TokenWebRepository tokenWebRepository;
    
    @Autowired
    private InstitutionRepository institutionRepository;
    
    @Autowired
    private AuthenticationManager authenticationManager;

    public UserWebService(
            UserWebRepository userWebRepository,
            InstitutionRepository institutionRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TokenWebRepository tokenWebRepository,
            AuthenticationManager authenticationManager,
            WebEmailService webEmailService) {
        this.userWebRepository = userWebRepository;
        this.institutionRepository = institutionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenWebRepository = tokenWebRepository;
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

        UserWeb userWeb = new UserWeb();
        userWeb.setName(request.getName());
        userWeb.setEmail(request.getEmail());
        userWeb.setLastName(request.getLastName());
        userWeb.setSecondLastName(request.getSecondLastName());
        userWeb.setPassword(passwordEncoder.encode(request.getPassword()));
        userWeb.setPosition(request.getPosition());
        userWeb.setRole(request.getRole() != null ? request.getRole() : RoleEnum.USER);
        userWeb.setAccountVerified(false);

        // Buscar la institución por su nombre
        Institution institution = institutionRepository.findByName(request.getInstitution())
        	    .orElseThrow(() -> new InstitutionNotFoundException("Institución no encontrada"));
        	userWeb.setInstitution(institution);

        	userWebRepository.save(userWeb);

        String verificationCode = webEmailService.sendVerificationUserWebEmail(userWeb);

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
    public AuthenticationResponse authenticateWeb(UserWebAuthenticationDTO request) {
    	UserWeb userWeb;
        if (request.getEmail() != null) {
        	userWeb = userWebRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Correo electrónico no encontrado. Por favor, verifica que tu correo esté registrado correctamente en la aplicación."));
        } else {
            throw new RuntimeException("Debe proporcionar correo electrónico");
        }
        if (!userWeb.isConfirmed()) {
            throw new RuntimeException("El usuario no está confirmado. Por favor, revise su correo electrónico para confirmar su cuenta.");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new RuntimeException("Debe proporcionar una contraseña");
        }
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                		userWeb.getEmail(),
                    request.getPassword()
                )
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        String token = jwtService.generateTokenUserWeb(userWeb);
        Collection<? extends GrantedAuthority> authorities = userWeb.getAuthorities();
        revokeAllTokenByUserWeb(userWeb);
        saveUserTokenWeb(token, userWeb);
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

    private UserWebDTO convertToDTO(UserWeb userWeb) {
        UserWebDTO dto = new UserWebDTO();
        dto.setId(userWeb.getId());
        dto.setName(userWeb.getName());
        dto.setLastName(userWeb.getLastName());
        dto.setSecondLastName(userWeb.getSecondLastName());
        dto.setEmail(userWeb.getEmail());
        dto.setPassword(userWeb.getPassword()); // Nota: normalmente no se devuelve la contraseña
        dto.setPosition(userWeb.getPosition());
        dto.setInstitution(userWeb.getInstitution().getName());
        dto.setRole(userWeb.getRole());
        dto.setAccountVerified(userWeb.isAccountVerified());
        return dto;
    }
    

    // 5. MÉTODOS AUXILIARES

    /**
     * REVOCA TODOS LOS TOKENS VÁLIDOS DEL USUARIO.
     *
     * ESTE MÉTODO MARCA TODOS LOS TOKENS VÁLIDOS DEL USUARIO COMO CERRADOS (LOGGED OUT).
     *
     * @param userWeb EL USUARIO CUYOS TOKENS SE DESEAN REVOCAR.
     */
    private void revokeAllTokenByUserWeb(UserWeb userWeb) {
        List<TokenWeb> validTokens = tokenWebRepository.findAllTokensByUser(userWeb.getId());
        if (validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(token -> {
            token.setLoggedOut(true);
        });

        tokenWebRepository.saveAll(validTokens);
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
     * @param userWeb EL USUARIO AL QUE PERTENECE EL TOKEN.
     */
    private void saveUserTokenWeb(String jwt, UserWeb userWeb) {
        List<TokenWeb> loggedOutTokens = tokenWebRepository.findAllByUserWebAndLoggedOut(userWeb, true);
        tokenWebRepository.deleteAll(loggedOutTokens);

        TokenWeb tokenWeb = new TokenWeb();
        tokenWeb.setToken(jwt);
        tokenWeb.setUserWeb(userWeb);
        tokenWeb.setExpirationDate(calculateExpireDate());
        tokenWeb.setLoggedOut(false);

        tokenWebRepository.save(tokenWeb);
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

        UserWeb userWeb = userWebRepository.findByEmail(email).orElse(null);
        if (userWeb == null) {
            return false;
        }

        // VERIFICAR EL CÓDIGO
        if (webEmailService.verifyCode(verificationCode)) {
            userWeb.setPassword(passwordEncoder.encode(newPassword));
            userWebRepository.save(userWeb);
            return true;
        }
        return false;
    }    
     
    //OBTENER POR ID PARA DTO TRANSFER
    public UserWebDTO getUserWebDTOById(Long id) throws Exception {
        UserWeb userWeb = getUserWebById(id);
        return convertToDTO(userWeb);
    }
    
 // ELIMINA UN USUARIO POR SU ID.
    @Transactional
    public void deleteById(Long id) {
        // BUSCA EL USUARIO INSTITUCIÓN POR SU ID O LANZA UNA EXCEPCIÓN SI NO SE ENCUENTRA
        UserWeb userWeb = userWebRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
        // BUSCA Y ELIMINA TODOS LOS TOKENS ASOCIADOS AL USUARIO INSTITUCIÓN
        List<TokenWeb> tokens = tokenWebRepository.findByUserWeb_id(id);
        tokenWebRepository.deleteAll(tokens);
        // ELIMINA LA REFERENCIA A LA INSTITUCIÓN PARA EVITAR LA VIOLACIÓN DE CLAVE FORÁNEA
        userWeb.setInstitution(null);
        // GUARDA EL USUARIO INSTITUCIÓN ACTUALIZADO PARA APLICAR EL CAMBIO
        userWebRepository.save(userWeb);
        // ELIMINA FÍSICAMENTE EL USUARIO INSTITUCIÓN DE LA BASE DE DATOS
        userWebRepository.delete(userWeb);
    }


    
    //OBTENER POR ID
    public UserWeb getUserWebById(Long id) throws Exception {
        return userWebRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));
    }
    
    //ACTUALIZAR UN USUARIO POR SU ID.
    public Response updateUserWeb(Long id, UserWebDTO request) throws Exception {
        UserWeb userWeb = getUserWebById(id);
        boolean emailChanged = false; // Verificar si el email está cambiando
        if (!Objects.equals(userWeb.getEmail(), request.getEmail())) { 
        if (userWebRepository.findByEmail(request.getEmail()).isPresent()) { throw new UserAlreadyExistsException("Ya existe un usuario registrado con este correo electrónico.");
        } emailChanged = true; }

        // Actualizar campos
        if (!Objects.equals(userWeb.getName(), request.getName())) userWeb.setName(request.getName());
        if (emailChanged) userWeb.setEmail(request.getEmail());
        if (!Objects.equals(userWeb.getLastName(), request.getLastName())) userWeb.setLastName(request.getLastName());
        if (!Objects.equals(userWeb.getSecondLastName(), request.getSecondLastName())) userWeb.setSecondLastName(request.getSecondLastName());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) userWeb.setPassword(passwordEncoder.encode(request.getPassword()));
        if (!Objects.equals(userWeb.getPosition(), request.getPosition())) userWeb.setPosition(request.getPosition());
        if (request.getRole() != null) {
        if (!Objects.equals(userWeb.getRole(), request.getRole())) userWeb.setRole(request.getRole()); } else {request.setRole(userWeb.getRole()); }        
       
        if (!Objects.equals(userWeb.getInstitution().getName(), request.getInstitution())) {
            Institution institution = institutionRepository.findByName(request.getInstitution())
                .orElseThrow(() -> new InstitutionNotFoundException("Institución no encontrada: " + request.getInstitution()));
            userWeb.setInstitution(institution);
        }
        if (emailChanged) {userWeb.setAccountVerified(false); }
        userWebRepository.save(userWeb);
        
        if (emailChanged) {
            String verificationCode = webEmailService.sendVerificationUserWebEmail(userWeb);
            return new Response("El usuario ha sido actualizado exitosamente. Se ha enviado un correo de verificación al nuevo email.");
        }
        return new Response("El usuario ha sido actualizado exitosamente.");
    }
}

