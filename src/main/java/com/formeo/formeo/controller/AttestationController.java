package com.formeo.formeo.controller;

import com.formeo.formeo.entity.Inscription;
import com.formeo.formeo.entity.Role;
import com.formeo.formeo.entity.Utilisateur;
import com.formeo.formeo.repository.InscriptionRepository;
import com.formeo.formeo.security.CustomUserDetails;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/attestations")
public class AttestationController {

    private final InscriptionRepository inscriptionRepository;

    public AttestationController(InscriptionRepository inscriptionRepository) {
        this.inscriptionRepository = inscriptionRepository;
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

    @GetMapping("/{inscriptionId}")
    public ResponseEntity<byte[]> downloadAttestation(@PathVariable Long inscriptionId) {
        Utilisateur current = getCurrentUser();

        Inscription ins = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscription introuvable"));

        if (!ins.getUtilisateur().getId().equals(current.getId())
                && current.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas autorisé à télécharger cette attestation");
        }

        if (ins.getNote() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Aucune évaluation enregistrée pour cette inscription");
        }

        boolean succes = ins.getNote() >= 10.0;
        String titre = succes ? "Attestation de succès" : "Attestation de présence";

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font textFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

            Paragraph pTitle = new Paragraph(titre, titleFont);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            pTitle.setSpacingAfter(20f);
            document.add(pTitle);

            Utilisateur eleve = ins.getUtilisateur();
            String nomComplet =
                    (eleve.getPrenom() != null ? eleve.getPrenom() + " " : "") +
                            (eleve.getNom() != null ? eleve.getNom() : "");

            String formationTitre = ins.getSession().getFormation() != null
                    ? ins.getSession().getFormation().getTitre()
                    : "Formation";

            String datesSession = "";
            if (ins.getSession().getDateDebut() != null && ins.getSession().getDateFin() != null) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                datesSession = "qui s'est déroulée du "
                        + ins.getSession().getDateDebut().toLocalDate().format(fmt)
                        + " au "
                        + ins.getSession().getDateFin().toLocalDate().format(fmt);
            }

            Paragraph p1 = new Paragraph(
                    "Nous certifions que " + nomComplet + " a suivi la formation :",
                    textFont
            );
            p1.setSpacingAfter(10f);
            document.add(p1);

            Paragraph p2 = new Paragraph(
                    "\"" + formationTitre + "\" " + datesSession + ".",
                    textFont
            );
            p2.setSpacingAfter(15f);
            document.add(p2);

            Paragraph p3 = new Paragraph(
                    "La note obtenue à l'évaluation finale est de "
                            + String.format("%.2f", ins.getNote()) + " / 20.",
                    textFont
            );
            p3.setSpacingAfter(10f);
            document.add(p3);

            Paragraph p4 = new Paragraph(
                    succes
                            ? "En conséquence, une attestation de succès est délivrée à l'intéressé(e)."
                            : "En conséquence, une attestation de présence est délivrée à l'intéressé(e).",
                    textFont
            );
            p4.setSpacingAfter(20f);
            document.add(p4);

            LocalDate today = LocalDate.now();
            DateTimeFormatter fmtDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            Paragraph pVilleDate = new Paragraph(
                    "Fait le " + today.format(fmtDate),
                    textFont
            );
            pVilleDate.setSpacingBefore(20f);
            pVilleDate.setSpacingAfter(40f);
            document.add(pVilleDate);

            Paragraph pSignature = new Paragraph(
                    "Signature et cachet de l'organisme de formation",
                    textFont
            );
            pSignature.setAlignment(Element.ALIGN_RIGHT);
            document.add(pSignature);

            document.close();

            byte[] pdf = baos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
            headers.add(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"attestation_" + inscriptionId + ".pdf\""
            );

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors de la génération du PDF"
            );
        }
    }
}
