package com.formeo.formeo.service;

import com.formeo.formeo.dto.LoginRequest;
import com.formeo.formeo.dto.LoginResponse;
import com.formeo.formeo.dto.RegisterRequest;
import com.formeo.formeo.entity.Role;
import com.formeo.formeo.entity.Utilisateur;
import com.formeo.formeo.repository.UtilisateurRepository;
import com.formeo.formeo.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UtilisateurRepository utilisateurRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Utilisateur register(RegisterRequest req) {
        if (utilisateurRepository.existsByEmailIgnoreCase(req.email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email deja utilise");
        }
        if (utilisateurRepository.existsByPseudoIgnoreCase(req.pseudo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pseudo deja utilise");
        }

        Utilisateur u = new Utilisateur();
        u.setNom(req.nom);
        u.setPrenom(req.prenom);
        u.setEmail(req.email);
        u.setPseudo(req.pseudo);
        u.setMotDePasse(passwordEncoder.encode(req.motDePasse));

        u.setTelephone(req.telephone);
        u.setEntreprise(req.entreprise);
        u.setAdressePostale(req.adressePostale);

        u.setRole(Role.USER);
        return utilisateurRepository.save(u);
    }

    public LoginResponse login(LoginRequest req) {
        Utilisateur u = utilisateurRepository.findByEmailIgnoreCase(req.email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));

        if (!passwordEncoder.matches(req.motDePasse, u.getMotDePasse())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }

        String token = jwtService.generateToken(u.getEmail(), u.getRole().name(), u.getId());

        return new LoginResponse(u.getId(), u.getEmail(), u.getPseudo(), u.getRole().name(), token);
    }
}
