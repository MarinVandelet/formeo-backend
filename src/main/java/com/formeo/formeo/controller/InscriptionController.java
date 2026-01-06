package com.formeo.formeo.controller;

import com.formeo.formeo.dto.InscriptionDto;
import com.formeo.formeo.entity.Inscription;
import com.formeo.formeo.mapper.InscriptionMapper;
import com.formeo.formeo.repository.InscriptionRepository;
import com.formeo.formeo.security.CustomUserDetails;
import com.formeo.formeo.service.InscriptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscriptions")
public class InscriptionController {

    private final InscriptionRepository inscriptionRepository;
    private final InscriptionService inscriptionService;

    public InscriptionController(InscriptionRepository inscriptionRepository,
                                 InscriptionService inscriptionService) {
        this.inscriptionRepository = inscriptionRepository;
        this.inscriptionService = inscriptionService;
    }

    // ADMIN : liste de toutes les inscriptions
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<InscriptionDto> listAll() {
        return inscriptionRepository.findAll()
                .stream()
                .map(InscriptionMapper::toDto)
                .toList();
    }

    // UTILISATEUR : liste de ses inscriptions
    @GetMapping("/me")
    public List<InscriptionDto> listPourMoi(Authentication authentication) {
        Long userId = getUserId(authentication);
        return inscriptionRepository.findByUtilisateurId(userId)
                .stream()
                .map(InscriptionMapper::toDto)
                .toList();
    }

    public record CreerInscriptionRequest(@NotNull Long sessionId) {}

    // UTILISATEUR : créer une inscription
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InscriptionDto creer(Authentication authentication, @Valid @RequestBody CreerInscriptionRequest req) {
        Long userId = getUserId(authentication);
        Inscription inscription = inscriptionService.creerInscription(userId, req.sessionId);
        return InscriptionMapper.toDto(inscription);
    }

    // UTILISATEUR : annuler une de ses inscriptions
    @DeleteMapping("/{id}")
    public InscriptionDto annuler(Authentication authentication, @PathVariable Long id) {
        Long userId = getUserId(authentication);
        Inscription inscription = inscriptionService.annulerInscription(userId, id);
        return InscriptionMapper.toDto(inscription);
    }

    // UTILISATEUR ou ADMIN : payer une inscription
    @PostMapping("/{id}/payer")
    @ResponseStatus(HttpStatus.OK)
    public InscriptionDto payer(Authentication authentication, @PathVariable Long id) {
        Long userId = getUserId(authentication);
        Inscription inscription = inscriptionService.payerInscription(userId, id);
        return InscriptionMapper.toDto(inscription);
    }

    private Long getUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getUtilisateur().getId();
        }
        throw new IllegalStateException("Utilisateur non authentifie correctement");
    }
}
