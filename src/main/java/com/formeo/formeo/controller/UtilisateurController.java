package com.formeo.formeo.controller;

import com.formeo.formeo.entity.Utilisateur;
import com.formeo.formeo.repository.UtilisateurRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping
    public List<Utilisateur> lister() {
        return utilisateurRepository.findAll();
    }

    @GetMapping("/{id}")
    public Utilisateur get(@PathVariable Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Utilisateur creer(@Valid @RequestBody Utilisateur utilisateur) {
        utilisateurRepository.findByEmailIgnoreCase(utilisateur.getEmail()).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email deja utilise");
        });
        return utilisateurRepository.save(utilisateur);
    }

    @PutMapping("/{id}")
    public Utilisateur modifier(@PathVariable Long id, @Valid @RequestBody Utilisateur body) {
        Utilisateur existing = get(id);

        if (!existing.getEmail().equalsIgnoreCase(body.getEmail())) {
            utilisateurRepository.findByEmailIgnoreCase(body.getEmail()).ifPresent(u -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email deja utilise");
            });
        }

        existing.setNom(body.getNom());
        existing.setPrenom(body.getPrenom());
        existing.setEmail(body.getEmail());
        existing.setTelephone(body.getTelephone());
        existing.setEntreprise(body.getEntreprise());

        return utilisateurRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable");
        }
        utilisateurRepository.deleteById(id);
    }
}
