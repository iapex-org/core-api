package com.iapex.enums;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum RoleEnum {
    // DEFINICIÓN DE LOS ROLES DISPONIBLES EN EL SISTEMA
    USER(List.of(new SimpleGrantedAuthority("USER"))),//user_mobile
    ADMIN(List.of(new SimpleGrantedAuthority("ADMIN"))),//super_admin
	EMPLOYEE(List.of(new SimpleGrantedAuthority("EMPLOYEE")));////user_web

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
            case "USER":
                return USER;
            case "EMPLOYEE":
                return EMPLOYEE;
            case "ADMIN":
                return ADMIN;
            default:
                throw new IllegalArgumentException("Role no reconocido: " + roleStr);
        }
    }
}