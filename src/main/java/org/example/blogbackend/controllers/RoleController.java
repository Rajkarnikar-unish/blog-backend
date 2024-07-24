package org.example.blogbackend.controllers;

import org.example.blogbackend.models.Role;
import org.example.blogbackend.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
