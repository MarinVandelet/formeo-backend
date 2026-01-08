package com.formeo.formeo.service;

import com.formeo.formeo.entity.*;
import com.formeo.formeo.dto.AttestationDto;
import com.formeo.formeo.repository.EmargementRepository;
import com.formeo.formeo.repository.EvaluationRepository;
import com.formeo.formeo.repository.SessionRepository;
import com.formeo.formeo.repository.UtilisateurRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

@Service
public class AttestationService {

    private final EvaluationRepository evaluationRepository;
    private final SessionRepository sessionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmargementRepository emargementRepository;

    public AttestationService(EvaluationRepository evaluationRepository,
                              SessionRepository sessionRepository,
                              UtilisateurRepository utilisateurRepository,
                              EmargementRepository emargementRepository) {
        this.evaluationRepository = evaluationRepository;
        this.sessionRepository = sessionRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.emargementRepository = emargementRepository;
    }

    public byte[] genererAttestationPdf(Long demandeurId, Long sessionId, Long utilisateurId) {

        Utilisateur demandeur = utilisateurRepository.findById(demandeurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demandeur introuvable"));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        Utilisateur eleve = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        Formation formation = session.getFormation();
        Utilisateur intervenant = formation.getIntervenant();

        boolean estAdmin = demandeur.getRole() == Role.ADMIN;
        boolean estEleveLuiMeme = demandeur.getId().equals(utilisateurId);
        boolean estIntervenant = (intervenant != null && intervenant.getId().equals(demandeurId));

        if (!(estAdmin || estEleveLuiMeme || estIntervenant)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas autorisé à générer cette attestation");
        }

        if (session.getDateFin() != null && session.getDateFin().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La session n'est pas encore terminée");
        }

        Evaluation evaluation = evaluationRepository.findBySessionIdAndUtilisateurId(sessionId, utilisateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Aucune évaluation trouvée pour cet utilisateur sur cette session"));

        boolean present = emargementRepository.existsByUtilisateurIdAndSessionId(utilisateurId, sessionId);
        if (!present) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'utilisateur n'a pas émargé cette session");
        }

        AttestationType type = (evaluation.getNote() != null && evaluation.getNote() > 10)
                ? AttestationType.SUCCES
                : AttestationType.PRESENCE;

        AttestationDto dto = new AttestationDto(
                session.getId(),
                eleve.getId(),
                eleve.getNom(),
                eleve.getPrenom(),
                formation.getTitre(),
                type,
                evaluation.getNote(),
                session.getDateDebut(),
                session.getDateFin(),
                session.getVille()
        );

        return createPdf(dto);
    }

    private byte[] createPdf(AttestationDto dto) {
        try {
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            // Titre
            Font titreFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph titre = new Paragraph("Attestation de " + labelType(dto.type), titreFont);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingAfter(20f);
            document.add(titre);

            // Corps
            Font normal = new Font(Font.HELVETICA, 12, Font.NORMAL);
            Font gras = new Font(Font.HELVETICA, 12, Font.BOLD);

            Paragraph p = new Paragraph();
            p.add(new Chunk("Je soussigné, ", normal));
            p.add(new Chunk("Formeo", gras));
            p.add(new Chunk(", atteste que ", normal));
            p.add(new Chunk(dto.utilisateurPrenom + " " + dto.utilisateurNom, gras));
            p.add(new Chunk(" a suivi la formation « " + dto.formationTitre + " »", normal));
            p.add(new Chunk(" organisée à " + dto.ville, normal));
            p.add(new Chunk(" du " + formatDate(dto.dateDebut) + " au " + formatDate(dto.dateFin) + ".", normal));
            p.setSpacingAfter(20f);
            document.add(p);

            Paragraph p2 = new Paragraph();
            p2.add(new Chunk("Note obtenue : ", gras));
            p2.add(new Chunk(dto.note != null ? dto.note.toString() + "/20" : "N/A", normal));
            p2.setSpacingAfter(10f);
            document.add(p2);

            Paragraph p3 = new Paragraph();
            if (dto.type == AttestationType.SUCCES) {
                p3.add(new Chunk("Cette attestation vaut ", normal));
                p3.add(new Chunk("attestation de SUCCÈS.", gras));
            } else {
                p3.add(new Chunk("Cette attestation vaut ", normal));
                p3.add(new Chunk("attestation de PRÉSENCE.", gras));
            }
            p3.setSpacingAfter(30f);
            document.add(p3);

            Paragraph dateLieu = new Paragraph(
                    "Fait à " + dto.ville + ", le " + formatDate(LocalDateTime.now()), normal);
            dateLieu.setAlignment(Element.ALIGN_RIGHT);
            document.add(dateLieu);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.toLocalDate().toString();
    }

    private String labelType(AttestationType type) {
        return type == AttestationType.SUCCES ? "succès" : "présence";
    }
}
