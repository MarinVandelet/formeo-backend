package com.formeo.formeo.controller;

import com.formeo.formeo.entity.Inscription;
import com.formeo.formeo.entity.Role;
import com.formeo.formeo.entity.Session;
import com.formeo.formeo.entity.StatutInscription;
import com.formeo.formeo.entity.Utilisateur;
import com.formeo.formeo.repository.InscriptionRepository;
import com.formeo.formeo.repository.SessionRepository;
import com.formeo.formeo.security.CustomUserDetails;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final InscriptionRepository inscriptionRepository;
    private final SessionRepository sessionRepository;

    public EvaluationController(InscriptionRepository inscriptionRepository,
                                SessionRepository sessionRepository) {
        this.inscriptionRepository = inscriptionRepository;
        this.sessionRepository = sessionRepository;
    }

    // DTO envoyé au front
    public static class EvaluationLine {
        public Long inscriptionId;
        public Long utilisateurId;
        public String prenom;
        public String nom;
        public String email;
        public Double note;
        public LocalDateTime dateEvaluation;

        public EvaluationLine(Long inscriptionId,
                              Long utilisateurId,
                              String prenom,
                              String nom,
                              String email,
                              Double note,
                              LocalDateTime dateEvaluation) {
            this.inscriptionId = inscriptionId;
            this.utilisateurId = utilisateurId;
            this.prenom = prenom;
            this.nom = nom;
            this.email = email;
            this.note = note;
            this.dateEvaluation = dateEvaluation;
        }
    }

    // DTO reçu du front
    public static class EvaluationUpdateRequest {
        @NotNull public Long inscriptionId;
        @NotNull public Double note;
    }

    private Utilisateur getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getUtilisateur();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
    }

    private void checkCanEvaluate(Utilisateur current) {
        if (current.getRole() == Role.ADMIN || current.getRole() == Role.INTERVENANT) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Vous n'êtes pas autorisé à évaluer des sessions");
    }

    @GetMapping("/session/{sessionId}")
    public List<EvaluationLine> listEvaluations(@PathVariable Long sessionId) {
        Utilisateur current = getCurrentUser();
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        checkCanEvaluate(current);

        List<Inscription> inscriptions = inscriptionRepository.findBySessionId(session.getId());

        return inscriptions.stream()
                .filter(ins -> ins.getStatut() == StatutInscription.PAYEE)
                .map(ins -> {
                    Utilisateur u = ins.getUtilisateur();
                    return new EvaluationLine(
                            ins.getId(),
                            u != null ? u.getId() : null,
                            u != null ? u.getPrenom() : null,
                            u != null ? u.getNom() : null,
                            u != null ? u.getEmail() : null,
                            ins.getNote(),
                            ins.getDateEvaluation()
                    );
                })
                .toList();
    }

    @PostMapping("/session/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveEvaluations(@PathVariable Long sessionId,
                                @RequestBody List<EvaluationUpdateRequest> body) {

        if (body == null || body.isEmpty()) {
            return;
        }

        Utilisateur current = getCurrentUser();
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        checkCanEvaluate(current);

        List<Long> ids = body.stream().map(r -> r.inscriptionId).distinct().toList();
        List<Inscription> inscriptions = inscriptionRepository.findAllById(ids);

        for (Inscription ins : inscriptions) {
            if (!ins.getSession().getId().equals(session.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Une inscription ne correspond pas à la session");
            }
            if (ins.getStatut() != StatutInscription.PAYEE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Seules les inscriptions payées peuvent être évaluées");
            }
        }

        LocalDateTime now = LocalDateTime.now();

        for (EvaluationUpdateRequest req : body) {
            Inscription ins = inscriptions.stream()
                    .filter(i -> i.getId().equals(req.inscriptionId))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Inscription introuvable: " + req.inscriptionId
                    ));

            if (req.note < 0 || req.note > 20) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La note doit être comprise entre 0 et 20");
            }

            ins.setNote(req.note);
            ins.setDateEvaluation(now);
            ins.setEvaluateur(current);
        }

        inscriptionRepository.saveAll(inscriptions);
    }
}
