package com.formeo.formeo.service;

import com.formeo.formeo.dto.FeuilleEmargementLigneDto;
import com.formeo.formeo.entity.*;
import com.formeo.formeo.repository.EmargementRepository;
import com.formeo.formeo.repository.InscriptionRepository;
import com.formeo.formeo.repository.SessionRepository;
import com.formeo.formeo.repository.UtilisateurRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmargementService {

    private final EmargementRepository emargementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SessionRepository sessionRepository;
    private final InscriptionRepository inscriptionRepository;

    public EmargementService(EmargementRepository emargementRepository,
                             UtilisateurRepository utilisateurRepository,
                             SessionRepository sessionRepository,
                             InscriptionRepository inscriptionRepository) {
        this.emargementRepository = emargementRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.sessionRepository = sessionRepository;
        this.inscriptionRepository = inscriptionRepository;
    }

    public Emargement emargerAujourdHui(Long utilisateurId, Long sessionId, String signatureBase64) {

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        boolean payee = inscriptionRepository.existsByUtilisateurIdAndSessionIdAndStatut(
                utilisateurId, sessionId, StatutInscription.PAYEE
        );
        if (!payee) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous devez avoir une inscription payée pour émarger cette session");
        }

        if (signatureBase64 == null || signatureBase64.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La signature est obligatoire pour émarger");
        }

        ZoneId zone = ZoneId.of("Europe/Paris");
        ZonedDateTime now = ZonedDateTime.now(zone);
        LocalDate today = now.toLocalDate();

        if (session.getDateDebut() == null || session.getDateFin() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Dates de session non définies");
        }

        LocalDate debut = session.getDateDebut().toLocalDate();
        LocalDate fin = session.getDateFin().toLocalDate();

        if (today.isBefore(debut) || today.isAfter(fin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ce jour n'est pas dans la période de la session");
        }

        DayOfWeek dow = today.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Impossible d'émarger un week-end");
        }

        LocalTime heure = now.toLocalTime();
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(18, 0);
        if (heure.isBefore(start) || heure.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Emargement possible uniquement entre 08h et 18h");
        }

        boolean dejaEmarge = emargementRepository
                .existsByUtilisateurIdAndSessionIdAndJourCours(utilisateurId, sessionId, today);
        if (dejaEmarge) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Vous avez déjà émargé pour aujourd'hui sur cette session");
        }

        Emargement emargement = new Emargement();
        emargement.setUtilisateur(utilisateur);
        emargement.setSession(session);
        emargement.setJourCours(today);
        emargement.setDateHeureEmargement(now.toLocalDateTime());
        emargement.setDateHeureSignature(now.toLocalDateTime());
        emargement.setSignatureBase64(signatureBase64);

        try {
            return emargementRepository.save(emargement);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Vous avez déjà émargé pour aujourd'hui sur cette session"
            );
        }
    }


    public List<Emargement> listPourUtilisateur(Long utilisateurId) {
        return emargementRepository.findByUtilisateurId(utilisateurId);
    }

    public List<Emargement> listPourSession(Long sessionId) {
        return emargementRepository.findBySessionId(sessionId);
    }

    public List<FeuilleEmargementLigneDto> feuillePourJour(Long demandeurId,
                                                           Long sessionId,
                                                           LocalDate jourCours) {

        Utilisateur demandeur = utilisateurRepository.findById(demandeurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        boolean estAdmin = demandeur.getRole() == Role.ADMIN;

        Utilisateur intervenant = null;
        if (session.getFormation() != null) {
            intervenant = session.getFormation().getIntervenant();
        }

        boolean estIntervenant = intervenant != null && intervenant.getId().equals(demandeurId);

        if (!(estAdmin || estIntervenant)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas intervenant/admin sur cette session");
        }

        if (session.getDateDebut() == null || session.getDateFin() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Dates de session non définies");
        }

        LocalDate debut = session.getDateDebut().toLocalDate();
        LocalDate fin = session.getDateFin().toLocalDate();

        if (jourCours.isBefore(debut) || jourCours.isAfter(fin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le jour " + jourCours.format(DateTimeFormatter.ISO_DATE)
                            + " n'est pas dans la période de la session");
        }

        DayOfWeek dow = jourCours.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ce jour est un week-end, pas un jour de cours");
        }

        List<Inscription> inscritsPayes =
                inscriptionRepository.findBySessionIdAndStatut(sessionId, StatutInscription.PAYEE);

        List<FeuilleEmargementLigneDto> lignes = new ArrayList<>();

        for (Inscription ins : inscritsPayes) {
            Utilisateur u = ins.getUtilisateur();
            Emargement em = emargementRepository
                    .findByUtilisateurIdAndSessionIdAndJourCours(u.getId(), sessionId, jourCours)
                    .orElse(null);

            boolean present = em != null;
            LocalDateTime dateEmarg = em != null ? em.getDateHeureEmargement() : null;

            lignes.add(new FeuilleEmargementLigneDto(
                    u.getId(),
                    u.getNom(),
                    u.getPrenom(),
                    u.getEmail(),
                    present,
                    jourCours,
                    dateEmarg
            ));
        }

        return lignes;
    }
}
