package com.iapex.service;

import java.util.Collection;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.iapex.config.AppUserDetails;
import com.iapex.model.User;
import com.iapex.model.UserInstitution;
import com.iapex.repository.UserInstitutionRepository;
import com.iapex.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class UserDetailsServiceImp implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserInstitutionRepository userInstitutionRepository;
    private final JwtService jwtService;

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    public UserDetailsServiceImp(UserRepository userRepository, UserInstitutionRepository userInstitutionRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.userInstitutionRepository = userInstitutionRepository;
        this.jwtService = jwtService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //logger.debug("Attempting to load user by email: {}", email);
        return loadUserByEmail(email);
    }

    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        //logger.debug("Loading user details for email: {}", email);
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            //logger.debug("User found in users table: {}", email);
            return userOptional.get();
        }

        Optional<UserInstitution> userInstitutionOptional = userInstitutionRepository.findByEmail(email);
        if (userInstitutionOptional.isPresent()) {
            //logger.debug("User found in user_institution table: {}", email);
            return userInstitutionOptional.get();
        }

        //logger.warn("User not found with email: {}", email);
        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    public UserDetails loadUserDetailsFromToken(String token) throws UsernameNotFoundException {
        // Extrae el email y las autoridades del token
        String email = jwtService.extractEmail(token); 
        Collection<GrantedAuthority> authorities = jwtService.extractAuthorities(token);

        // Busca al usuario en el repositorio
        AppUserDetails appUserDetails = (AppUserDetails) loadUserByEmail(email);

        // Crea y devuelve un objeto UserDetails
        return new org.springframework.security.core.userdetails.User(appUserDetails.getEmail(), appUserDetails.getPassword(), authorities);
    }
}
