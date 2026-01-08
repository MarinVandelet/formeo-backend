package com.formeo.formeo.service;

import com.formeo.formeo.entity.*;
import com.formeo.formeo.repository.InscriptionRepository;
import com.formeo.formeo.repository.SessionRepository;
import com.formeo.formeo.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SessionRepository sessionRepository;

    public InscriptionService(InscriptionRepository inscriptionRepository,
                              UtilisateurRepository utilisateurRepository,
                              SessionRepository sessionRepository) {
        this.inscriptionRepository = inscriptionRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.sessionRepository = sessionRepository;
    }

    public Inscription creerInscription(Long utilisateurId, Long sessionId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        long nbInscrits = inscriptionRepository.countBySessionIdAndStatutIn(
                sessionId,
                List.of(StatutInscription.EN_ATTENTE, StatutInscription.PAYEE)
        );
        if (nbInscrits >= session.getCapacite()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Session complete (capacite atteinte)");
        }

        long chevauchements = inscriptionRepository.countInscriptionsChevauchantes(
                utilisateurId,
                session.getDateDebut(),
                session.getDateFin(),
                List.of(StatutInscription.EN_ATTENTE, StatutInscription.PAYEE)
        );
        if (chevauchements > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Impossible: inscription simultanee detectee");
        }

        Inscription inscription = new Inscription();
        inscription.setUtilisateur(utilisateur);
        inscription.setSession(session);
        inscription.setStatut(StatutInscription.EN_ATTENTE);

        return inscriptionRepository.save(inscription);
    }

    public Inscription annulerInscription(Long utilisateurId, Long inscriptionId) {
        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscription introuvable"));

        if (!inscription.getUtilisateur().getId().equals(utilisateurId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez annuler que vos propres inscriptions");
        }

        if (inscription.getStatut() == StatutInscription.ANNULEE) {
            return inscription;
        }

        inscription.setStatut(StatutInscription.ANNULEE);
        return inscriptionRepository.save(inscription);
    }

    public Inscription payerInscription(Long demandeurId, Long inscriptionId) {
        Utilisateur demandeur = utilisateurRepository.findById(demandeurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscription introuvable"));

        boolean estAdmin = demandeur.getRole() == Role.ADMIN;
        boolean estProprietaire = inscription.getUtilisateur().getId().equals(demandeurId);

        if (!(estAdmin || estProprietaire)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous ne pouvez payer que vos propres inscriptions");
        }

        if (inscription.getStatut() == StatutInscription.ANNULEE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Impossible de payer une inscription annulee");
        }

        inscription.setStatut(StatutInscription.PAYEE);
        return inscriptionRepository.save(inscription);
    }
}
