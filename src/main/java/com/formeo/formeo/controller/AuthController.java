package com.formeo.formeo.controller;

import com.formeo.formeo.dto.LoginRequest;
import com.formeo.formeo.dto.LoginResponse;
import com.formeo.formeo.dto.RegisterRequest;
import com.formeo.formeo.entity.Utilisateur;
import com.formeo.formeo.security.CustomUserDetails;
import com.formeo.formeo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Utilisateur register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    // ✅ DTO de réponse
    public static class MeResponse {
        public Long id;
        public String email;
        public String role;

        public MeResponse(Long id, String email, String role) {
            this.id = id;
            this.email = email;
            this.role = role;
        }
    }

    // ✅ Authenticated user info
    @PreAuthorize("hasAnyRole('USER','ADMIN','INTERVENANT')")
    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            Utilisateur u = cud.getUtilisateur();
            return new MeResponse(u.getId(), u.getEmail(), u.getRole().name());
        }
        throw new IllegalStateException("Utilisateur non authentifie correctement");
    }
}
