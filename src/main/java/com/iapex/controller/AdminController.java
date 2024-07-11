package com.iapex.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iapex.dto.UserDTO;
import com.iapex.service.TokenService;
import com.iapex.service.UserService;


@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8100", "http://localhost:8101"})
@RequestMapping("/admin")
@RestController
public class AdminController {

    @Autowired
    private final UserService userService;
    private final TokenService tokenService;

    public AdminController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    // ENDPOINT PARA OBTENER TODOS LOS USUARIOS DE LA APP MOVIL
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/getAllUsers")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> userDtos = userService.getAllUsers().stream()
                                    .map(user -> {
                                        UserDTO dto = new UserDTO();
                                        dto.setIdUser(user.getIdUser());
                                        dto.setEmail(user.getEmail());
                                        dto.setPhone(user.getPhone());
                                        dto.setStatus(user.isConfirmed());
                                        dto.setRole(user.getRole());
                                        return dto;
                                    })
                                    .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    // ENDPOINT PARA ELIMINAR USUARIO
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/deleteUserById/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long id) {
        // Eliminar los tokens asociados al usuario
        tokenService.deleteTokensByUserId(id);
        // Eliminar el usuario
        userService.deleteById(id);
        return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
    }

}
