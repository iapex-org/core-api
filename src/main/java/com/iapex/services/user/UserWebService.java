package com.iapex.services.user;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
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
import com.iapex.exceptions.AuthenticateEmailException;
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
     * ESTE MÉTODO VERIFICA SI YA EXISTE UN USUARIO CON EL CORREO ELECTRÓNICO
     * PROPORCIONADO.
     * SI NO EXISTE, CREA UN NUEVO USUARIO, LO GUARDA EN EL REPOSITORIO Y ENVÍA UN
     * CORREO
     * ELECTRÓNICO DE VERIFICACIÓN.
     *
     * @param request LOS DATOS DEL USUARIO QUE SE DESEA REGISTRAR.
     * @return UNA RESPUESTA INDICANDO QUE EL REGISTRO FUE EXITOSO Y QUE SE DEBE
     *         VERIFICAR EL CORREO ELECTRÓNICO.
     * @throws Exception SI YA EXISTE UN USUARIO CON EL CORREO ELECTRÓNICO
     *                   PROPORCIONADO.
     */
    public Response registerUser(UserWebDTO request) {
        if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Debe proporcionar un correo válido.");
        }
    
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
    
        if (userWebRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("El correo ya está registrado.");
        }
    
        UserWeb userWeb = new UserWeb();
        userWeb.setName(request.getName());
        userWeb.setEmail(request.getEmail());
        userWeb.setLastName(request.getLastName());
        userWeb.setSecondLastName(request.getSecondLastName());
        userWeb.setPassword(passwordEncoder.encode(request.getPassword())); // Contraseña segura
        userWeb.setPosition(request.getPosition());
        userWeb.setRole(request.getRole() != null ? request.getRole() : RoleEnum.USER_WEB);
        userWeb.setAccountVerified(false);
    
        Institution institution = institutionRepository.findByName(request.getInstitution())
                .orElseThrow(() -> new InstitutionNotFoundException("Institución no encontrada"));
        userWeb.setInstitution(institution);
    
        userWebRepository.save(userWeb);
        CompletableFuture.runAsync(() -> webEmailService.sendVerificationEmailAsync(userWeb));
    
        return new Response("Registro exitoso. Verifique su correo.");
    }    

    /**
     * AUTENTICA A UN USUARIO.
     * 
     * ESTE MÉTODO VERIFICA LAS CREDENCIALES DEL USUARIO Y GENERA UN TOKEN JWT SI LA
     * AUTENTICACIÓN
     * ES EXITOSA. TAMBIÉN REVISA SI EL USUARIO ESTÁ AUTENTICADO Y SI LA CONTRASEÑA
     * ES CORRECTA.
     * 
     * @param request LOS DATOS DE AUTENTICACIÓN DEL USUARIO.
     * @return UNA RESPUESTA DE AUTENTICACIÓN CON EL TOKEN JWT Y UN MENSAJE DE
     *         ÉXITO.
     * @throws RuntimeException SI EL CORREO ELECTRÓNICO O LA CONTRASEÑA SON
     *                          INCORRECTOS, O SI EL USUARIO NO ESTÁ AUTENTICADO.
     */
    public AuthenticationResponse authenticateWeb(UserWebAuthenticationDTO request) {
        if (request.getEmail() == null || request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new BadCredentialsException("Debe proporcionar un correo y una contraseña válidos.");
        }
    
        UserWeb userWeb = userWebRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Correo electrónico no encontrado."));
    
        if (!userWeb.isConfirmed()) {
            throw new AuthenticateEmailException("Debe verificar su cuenta antes de iniciar sesión.");
        }
    
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    
        String token = jwtService.generateTokenUserWeb(userWeb);
        Collection<? extends GrantedAuthority> authorities = userWeb.getAuthorities();
    
        revokeAllTokenByUserWeb(userWeb);
        saveUserTokenWeb(token, userWeb);
    
        return new AuthenticationResponse(token, "Inicio de sesión exitoso", authorities);
    }
    

    /**
     * OBTIENE TODOS LOS USUARIOS INSTITUCIONALES.
     *
     * ESTE MÉTODO RECUPERA TODOS LOS USUARIOS INSTITUCIONALES ALMACENADOS EN LA
     * BASE DE DATOS.
     *
     * @return UNA LISTA DE TODOS LOS USUARIOS INSTITUCIONALES.
     */
    public List<UserWebDTO> getAllUserDTOs() {
        List<UserWeb> users = userWebRepository.findAll();
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // OBTIENE TODOS LOS USUARIOS DE LA INSTITUCIÓN DEL USUARIO AUTENTICADO.
    public List<UserWebDTO> getUsersFromSameInstitution(String authenticatedEmail) {
        UserWeb authenticatedUser = userWebRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        List<UserWeb> users = userWebRepository.findByInstitution(authenticatedUser.getInstitution());

        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserWebDTO convertToDTO(UserWeb userWeb) {
        UserWebDTO dto = new UserWebDTO();
        dto.setId(userWeb.getId());
        dto.setName(userWeb.getName());
        dto.setLastName(userWeb.getLastName());
        dto.setSecondLastName(userWeb.getSecondLastName());
        dto.setEmail(userWeb.getEmail());
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
     * ESTE MÉTODO MARCA TODOS LOS TOKENS VÁLIDOS DEL USUARIO COMO CERRADOS (LOGGED
     * OUT).
     *
     * @param userWeb EL USUARIO CUYOS TOKENS SE DESEAN REVOCAR.
     */
    private void revokeAllTokenByUserWeb(UserWeb userWeb) {
        List<TokenWeb> validTokens = tokenWebRepository.findAllTokensByUser(userWeb.getId());
        if (!validTokens.isEmpty()) {
            tokenWebRepository.deleteAll(validTokens); // Eliminar tokens en lugar de solo marcarlos como cerrados
        }
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
     * @param jwt     EL TOKEN JWT QUE SE VA A GUARDAR.
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
        calendar.add(Calendar.MINUTE, 1000000);
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

    // OBTENER POR ID PARA DTO TRANSFER
    public UserWebDTO getUserWebDTOById(Long id) throws Exception {
        UserWeb userWeb = getUserWebById(id);
        return convertToDTO(userWeb);
    }

    // ELIMINA UN USUARIO POR SU ID.
    @Transactional
    public void deleteById(Long id) {
        // BUSCA EL USUARIO INSTITUCIÓN POR SU ID O LANZA UNA EXCEPCIÓN SI NO SE
        // ENCUENTRA
        UserWeb userWeb = userWebRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
        // BUSCA Y ELIMINA TODOS LOS TOKENS ASOCIADOS AL USUARIO INSTITUCIÓN
        List<TokenWeb> tokens = tokenWebRepository.findByUserWeb_id(id);
        tokenWebRepository.deleteAll(tokens);
        // ELIMINA LA REFERENCIA A LA INSTITUCIÓN PARA EVITAR LA VIOLACIÓN DE CLAVE
        // FORÁNEA
        userWeb.setInstitution(null);
        // GUARDA EL USUARIO INSTITUCIÓN ACTUALIZADO PARA APLICAR EL CAMBIO
        userWebRepository.save(userWeb);
        // ELIMINA FÍSICAMENTE EL USUARIO INSTITUCIÓN DE LA BASE DE DATOS
        userWebRepository.delete(userWeb);
    }

    // OBTENER POR ID
    public UserWeb getUserWebById(Long id) throws Exception {
        return userWebRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));
    }

    // ACTUALIZAR UN USUARIO POR SU ID.
    public Response updateUserWeb(Long id, UserWebDTO request) throws Exception {
        UserWeb userWeb = getUserWebById(id);
    
        if (!Objects.equals(userWeb.getEmail(), request.getEmail()) && userWebRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Correo ya registrado.");
        }
    
        userWeb.setName(request.getName());
        userWeb.setLastName(request.getLastName());
        userWeb.setSecondLastName(request.getSecondLastName());
    
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            userWeb.setPassword(passwordEncoder.encode(request.getPassword()));
        }
    
        userWeb.setPosition(request.getPosition());
        
        if (request.getRole() != null && userWeb.getRole() != RoleEnum.SUPER_ADMIN) { 
            userWeb.setRole(request.getRole());
        }
    
        Institution institution = institutionRepository.findByName(request.getInstitution())
                .orElseThrow(() -> new InstitutionNotFoundException("Institución no encontrada"));
        userWeb.setInstitution(institution);
    
        userWebRepository.save(userWeb);
        return new Response("Usuario actualizado exitosamente.");
    }
    
}