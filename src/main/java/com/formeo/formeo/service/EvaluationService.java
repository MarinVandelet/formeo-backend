package com.formeo.formeo.service;

import com.formeo.formeo.entity.*;
import com.formeo.formeo.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final SessionRepository sessionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmargementRepository emargementRepository;
    private final InscriptionRepository inscriptionRepository;

    public EvaluationService(EvaluationRepository evaluationRepository,
                             SessionRepository sessionRepository,
                             UtilisateurRepository utilisateurRepository,
                             EmargementRepository emargementRepository,
                             InscriptionRepository inscriptionRepository) {
        this.evaluationRepository = evaluationRepository;
        this.sessionRepository = sessionRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.emargementRepository = emargementRepository;
        this.inscriptionRepository = inscriptionRepository;
    }

    public boolean peutGererSession(Long utilisateurId, Long sessionId) {
        Utilisateur user = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        // admin voit tout
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        Formation formation = session.getFormation();
        Utilisateur intervenant = formation.getIntervenant();

        return intervenant != null && intervenant.getId().equals(utilisateurId);
    }

    public Evaluation enregistrerEvaluation(Long evaluateurId,
                                            Long sessionId,
                                            Long utilisateurId,
                                            Integer note,
                                            String commentaire) {

        if (!peutGererSession(evaluateurId, sessionId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas l'intervenant (ou admin) de cette formation");
        }

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        Utilisateur eleve = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        // Verif que l'eleve rentre dans les conditions
        boolean payee = inscriptionRepository.existsByUtilisateurIdAndSessionIdAndStatut(
                utilisateurId, sessionId, StatutInscription.PAYEE
        );
        if (!payee) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'utilisateur n'est pas inscrit et payé pour cette session");
        }

        // Vérifier qu'il a émargé
        boolean present = emargementRepository.existsByUtilisateurIdAndSessionId(utilisateurId, sessionId);
        if (!present) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'utilisateur n'a pas émargé cette session");
        }

        Evaluation evaluation = evaluationRepository
                .findBySessionIdAndUtilisateurId(sessionId, utilisateurId)
                .orElseGet(Evaluation::new);

        evaluation.setSession(session);
        evaluation.setUtilisateur(eleve);
        evaluation.setNote(note);
        evaluation.setCommentaire(commentaire);
        evaluation.setDateEvaluation(LocalDateTime.now());

        return evaluationRepository.save(evaluation);
    }

    public List<Evaluation> getEvaluationsPourSession(Long sessionId) {
        return evaluationRepository.findBySessionId(sessionId);
    }

    public List<Evaluation> getEvaluationsPourUtilisateur(Long utilisateurId) {
        return evaluationRepository.findByUtilisateurId(utilisateurId);
    }
}
