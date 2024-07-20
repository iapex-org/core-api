package com.iapex.enums;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum RoleEnum {
    // DEFINICIÓN DE LOS ROLES DISPONIBLES EN EL SISTEMA
    USER_MOBILE(List.of(new SimpleGrantedAuthority("USER_MOBILE"))),
    SUPER_ADMIN(List.of(new SimpleGrantedAuthority("SUPER_ADMIN"))),
    USER_WEB(List.of(new SimpleGrantedAuthority("USER_WEB")));////
    // CAMPO PARA ALMACENAR LAS AUTORIDADES ASOCIADAS A CADA ROL
    private final List<GrantedAuthority> authorities;

    // CONSTRUCTOR DEL ENUM
    RoleEnum(List<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    // MÉTODO PARA OBTENER LAS AUTORIDADES DE UN ROL
    public List<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // MÉTODO ESTÁTICO PARA CONVERTIR UNA CADENA EN UN OBJETO ROLE
    public static RoleEnum fromString(String roleStr) {
        switch (roleStr.toUpperCase()) {
            case "USER_MOBILE":
                return USER_MOBILE;
            case "USER_WEB":
                return USER_WEB;
            case "SUPER_ADMIN":
                return SUPER_ADMIN;
            default:
                throw new IllegalArgumentException("Role no reconocido: " + roleStr);
        }
    }
}