package com.seams.backend.web.controller;

import com.seams.backend.core.model.Role;
import com.seams.backend.core.model.User;
import com.seams.backend.core.repository.UserRepository;
import com.seams.backend.application.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/supervisor")
public class SupervisorController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public SupervisorController(UserRepository userRepository, PasswordEncoder passwordEncoder, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @GetMapping("/admins")
    @PreAuthorize("hasAuthority('SUPERVISOR')")
    public List<User> getAdmins() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .collect(Collectors.toList());
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('SUPERVISOR')")
    public User createUser(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @DeleteMapping("/admins/{id}")
    @PreAuthorize("hasAuthority('SUPERVISOR')")
    public void deleteAdmin(@PathVariable Integer id) {
        userService.delete(id);
    }
}
