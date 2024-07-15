package com.iapex.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iapex.model.User;
import com.iapex.service.UserService;


@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8100", "http://localhost:8101"})
@RequestMapping("/admin")
@RestController
public class AdminController {

    @Autowired
    private final UserService userService;

    public AdminController(UserService userService ) {
        this.userService = userService;
    }

    // ENDPOINT PARA OBTENER TODOS LOS USUARIOS
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/getAllUsers")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }



}
