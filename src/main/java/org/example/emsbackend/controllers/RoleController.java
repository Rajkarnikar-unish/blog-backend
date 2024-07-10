package org.example.emsbackend.controllers;

import org.example.emsbackend.models.Role;
import org.example.emsbackend.repositories.RoleRepository;
import org.example.emsbackend.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/all")
    public ResponseEntity<?> getAllRoles() {
        List<Role> roles = roleRepository.findAll();

        return ResponseEntity.ok(roles);
    }


}
