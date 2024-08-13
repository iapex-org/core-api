package com.iapex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.iapex.filter.JwtAuthenticationFilter;
import com.iapex.services.security.UserDetailsServiceImp;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableScheduling
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UserDetailsServiceImp userDetailsServiceImp;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomLogoutHandler logoutHandler;

    public SecurityConfig(UserDetailsServiceImp userDetailsServiceImp,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomLogoutHandler logoutHandler) {
        this.userDetailsServiceImp = userDetailsServiceImp;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.logoutHandler = logoutHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        req->req.requestMatchers(
                        						"/media/**",
                        						"/auth/**",
                        						"/admin/**",
                        						"/users/**",
                        						"/institutions/**",
                        						"/employeeInstitution/**",
                        						"/userWeb/**",
                        						"/adminInstitution/**",
                        						"/v3/api-docs/**", 
                        						"/swagger-ui/**",
                        						"/memberships/**", 
                        						"/conversations/**",
                        						"/api/v1/**",
                        						"/api/v1/institutions/**",
                        						"/patients/**", 
                        						"/acess/**", 
                        						"/swagger-ui.html")
                        
                                .permitAll()
                                .requestMatchers("/protected-route").authenticated()

                                .anyRequest()
                                .authenticated()
                ).userDetailsService(userDetailsServiceImp)
                .sessionManagement(session->session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(
                	    e -> e.accessDeniedHandler(
                	            (request, response, accessDeniedException) -> {
                	                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                	                response.getWriter().write("Acceso denegado, solo los usuarios estan autorizados");
                	            }
                	        )
                	        .authenticationEntryPoint(
                	            (request, response, authException) -> {
                	                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                	                response.getWriter().write("No estas autorizado, inicia sesion");
                	            }
                	        )
                	)
                .logout(l -> l
                        .logoutUrl("/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            SecurityContextHolder.clearContext(); 
                            response.setStatus(HttpServletResponse.SC_OK); 
                            response.getWriter().write("Has cerrado sesion exitosamente.");  
                            })
                )
                .build();
		}
    
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}

