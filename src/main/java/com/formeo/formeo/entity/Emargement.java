package com.formeo.formeo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "emargement",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_emargement_user_session_jour",
                        columnNames = {"utilisateur_id", "session_id", "jour_cours"}
                )
        }
)
public class Emargement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(name = "date_heure_signature", nullable = false)
    private LocalDateTime dateHeureSignature = LocalDateTime.now();

    @Column(name = "date_heure_emargement", nullable = false)
    private LocalDateTime dateHeureEmargement;

    @Column(name = "jour_cours", nullable = false)
    private LocalDate jourCours;

    @Lob
    @Column(name = "signature_base64", nullable = false, columnDefinition = "LONGTEXT")
    private String signatureBase64 = "";

    public Emargement() {
    }


    public Long getId() {
        return id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public LocalDateTime getDateHeureSignature() {
        return dateHeureSignature;
    }

    public void setDateHeureSignature(LocalDateTime dateHeureSignature) {
        this.dateHeureSignature = dateHeureSignature;
    }

    public LocalDateTime getDateHeureEmargement() {
        return dateHeureEmargement;
    }

    public void setDateHeureEmargement(LocalDateTime dateHeureEmargement) {
        this.dateHeureEmargement = dateHeureEmargement;
    }

    public LocalDate getJourCours() {
        return jourCours;
    }

    public void setJourCours(LocalDate jourCours) {
        this.jourCours = jourCours;
    }

    public String getSignatureBase64() {
        return signatureBase64;
    }

    public void setSignatureBase64(String signatureBase64) {
        this.signatureBase64 = signatureBase64;
    }
}
