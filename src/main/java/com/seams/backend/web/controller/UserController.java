package com.seams.backend.web.controller;

import com.seams.backend.core.model.User;
import com.seams.backend.application.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public Page<User> getAll(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.findAll(search, PageRequest.of(page, size));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPERVISOR')")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    @PostMapping("/{id}/reset")
    @PreAuthorize("hasAuthority('SUPERVISOR')")
    public void forceReset(@PathVariable Integer id) {
        service.forcePasswordReset(id);
    }
}
