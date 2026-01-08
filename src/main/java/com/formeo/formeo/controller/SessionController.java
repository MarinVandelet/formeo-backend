package com.formeo.formeo.controller;

import com.formeo.formeo.dto.SessionDto;
import com.formeo.formeo.entity.Formation;
import com.formeo.formeo.entity.Session;
import com.formeo.formeo.entity.StatutInscription;
import com.formeo.formeo.mapper.DtoMapper;
import com.formeo.formeo.repository.FormationRepository;
import com.formeo.formeo.repository.InscriptionRepository;
import com.formeo.formeo.repository.SessionRepository;
import com.formeo.formeo.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionRepository sessionRepository;
    private final FormationRepository formationRepository;
    private final InscriptionRepository inscriptionRepository;

    public SessionController(SessionRepository sessionRepository,
                             FormationRepository formationRepository,
                             InscriptionRepository inscriptionRepository) {
        this.sessionRepository = sessionRepository;
        this.formationRepository = formationRepository;
        this.inscriptionRepository = inscriptionRepository;
    }

    private Long getUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getUtilisateur().getId();
        }
        throw new IllegalStateException("Utilisateur non authentifie correctement");
    }

    // PUBLIC
    @GetMapping
    public List<SessionDto> lister(@RequestParam(required = false) Long formationId) {
        List<Session> sessions = (formationId != null)
                ? sessionRepository.findByFormationId(formationId)
                : sessionRepository.findAll();

        return sessions.stream().map(DtoMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public SessionDto get(@PathVariable Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));
        return DtoMapper.toDto(session);
    }

    // Intervenant / Admin : sessions dont il est intervenant
    @GetMapping("/mes-sessions")
    @PreAuthorize("hasAnyRole('INTERVENANT','ADMIN')")
    public List<SessionDto> mesSessions(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<Session> sessions = sessionRepository.findByFormationIntervenantId(userId);
        return sessions.stream().map(DtoMapper::toDto).toList();
    }

    @GetMapping("/disponibles")
    public List<SessionDisponibleResponse> sessionsDisponibles() {
        return sessionRepository.findAll()
                .stream()
                .map(this::toDisponible)
                .toList();
    }

    private SessionDisponibleResponse toDisponible(Session session) {
        List<StatutInscription> statutsComptes = List.of(
                StatutInscription.PAYEE,
                StatutInscription.EN_ATTENTE
        );

        long inscrits = inscriptionRepository.countBySessionIdAndStatutIn(
                session.getId(), statutsComptes
        );

        int placesRestantes = session.getCapacite() - (int) inscrits;
        if (placesRestantes < 0) placesRestantes = 0;

        return new SessionDisponibleResponse(
                session.getId(),
                session.getCapacite(),
                placesRestantes
        );
    }

    public static class SessionDisponibleResponse {
        public Long sessionId;
        public int capacite;
        public int placesRestantes;

        public SessionDisponibleResponse(Long sessionId, int capacite, int placesRestantes) {
            this.sessionId = sessionId;
            this.capacite = capacite;
            this.placesRestantes = placesRestantes;
        }
    }

    // admin
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionDto creer(@Valid @RequestBody Session session) {
        Long formationId = session.getFormation() != null ? session.getFormation().getId() : null;
        if (formationId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "formation.id est obligatoire");
        }

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formation inexistante"));

        if (session.getDateDebut() != null && session.getDateFin() != null &&
                !session.getDateDebut().isBefore(session.getDateFin())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dateDebut doit etre avant dateFin");
        }

        session.setFormation(formation);
        return DtoMapper.toDto(sessionRepository.save(session));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public SessionDto modifier(@PathVariable Long id, @Valid @RequestBody Session body) {
        Session existing = sessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        existing.setDateDebut(body.getDateDebut());
        existing.setDateFin(body.getDateFin());
        existing.setCapacite(body.getCapacite());
        existing.setVille(body.getVille());
        existing.setAdresse(body.getAdresse());
        existing.setSalle(body.getSalle());

        if (body.getFormation() != null) {
            Formation formation = formationRepository.findById(body.getFormation().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formation inexistante"));
            existing.setFormation(formation);
        }

        if (existing.getDateDebut() != null && existing.getDateFin() != null &&
                !existing.getDateDebut().isBefore(existing.getDateFin())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dateDebut doit etre avant dateFin");
        }

        return DtoMapper.toDto(sessionRepository.save(existing));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable");
        }
        sessionRepository.deleteById(id);
    }
}
