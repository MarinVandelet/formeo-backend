package com.formeo.formeo.controller;

import com.formeo.formeo.dto.EmargementDto;
import com.formeo.formeo.dto.FeuilleEmargementLigneDto;
import com.formeo.formeo.entity.Emargement;
import com.formeo.formeo.mapper.EmargementMapper;
import com.formeo.formeo.security.CustomUserDetails;
import com.formeo.formeo.service.EmargementService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/emargements")
public class EmargementController {

    private final EmargementService emargementService;

    public EmargementController(EmargementService emargementService) {
        this.emargementService = emargementService;
    }

    private Long getUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getUtilisateur().getId();
        }
        throw new IllegalStateException("Utilisateur non authentifié correctement");
    }

    public record EmargementRequest(Long sessionId, String signatureBase64) {}

    // Élève : émarger aujourd'hui (avec signature)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmargementDto emarger(Authentication authentication,
                                 @RequestBody EmargementRequest req) {
        if (req.sessionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sessionId est obligatoire");
        }
        Long userId = getUserId(authentication);
        Emargement e = emargementService.emargerAujourdHui(userId, req.sessionId(), req.signatureBase64());
        return EmargementMapper.toDto(e);
    }

    // Élève : ses émargements
    @GetMapping("/me")
    public List<EmargementDto> mesEmargements(Authentication authentication) {
        Long userId = getUserId(authentication);
        return emargementService.listPourUtilisateur(userId)
                .stream()
                .map(EmargementMapper::toDto)
                .toList();
    }

    //  Intervenant /  Admin : feuille d'émargement selection par jour
    @GetMapping("/session/{sessionId}/jour/{jour}")
    @PreAuthorize("hasAnyRole('INTERVENANT','ADMIN')")
    public List<FeuilleEmargementLigneDto> feuillePourJour(Authentication authentication,
                                                           @PathVariable Long sessionId,
                                                           @PathVariable String jour) {
        Long demandeurId = getUserId(authentication);
        LocalDate date;
        try {
            date = LocalDate.parse(jour);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Format de date invalide (attendu: YYYY-MM-DD)");
        }
        return emargementService.feuillePourJour(demandeurId, sessionId, date);
    }

    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyRole('INTERVENANT','ADMIN')")
    public List<EmargementDto> emargementsPourSession(@PathVariable Long sessionId) {
        return emargementService.listPourSession(sessionId)
                .stream()
                .map(EmargementMapper::toDto)
                .toList();
    }
}
