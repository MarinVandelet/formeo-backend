package com.formeo.formeo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "inscription",
        uniqueConstraints = {
                // Empêche le même utilisateur de s'inscrire 2 fois à la même session
                @UniqueConstraint(
                        name = "uk_inscription_user_session",
                        columnNames = {"utilisateur_id", "session_id"}
                )
        }
)
public class Inscription {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutInscription statut = StatutInscription.EN_ATTENTE;

    @Column(nullable = false)
    private LocalDateTime creeLe = LocalDateTime.now();

    // ⭐ NOUVEAU : note finale (0–20)
    @Column(name = "note")
    private Double note;

    // ⭐ NOUVEAU : date de l'évaluation
    @Column(name = "date_evaluation")
    private LocalDateTime dateEvaluation;

    // ⭐ NOUVEAU : intervenant qui a évalué
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluateur_id")
    private Utilisateur evaluateur;

    public Inscription() {
    }

    // --- getters / setters ---

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

    public StatutInscription getStatut() {
        return statut;
    }

    public void setStatut(StatutInscription statut) {
        this.statut = statut;
    }

    public LocalDateTime getCreeLe() {
        return creeLe;
    }

    public void setCreeLe(LocalDateTime creeLe) {
        this.creeLe = creeLe;
    }

    public Double getNote() {
        return note;
    }

    public void setNote(Double note) {
        this.note = note;
    }

    public LocalDateTime getDateEvaluation() {
        return dateEvaluation;
    }

    public void setDateEvaluation(LocalDateTime dateEvaluation) {
        this.dateEvaluation = dateEvaluation;
    }

    public Utilisateur getEvaluateur() {
        return evaluateur;
    }

    public void setEvaluateur(Utilisateur evaluateur) {
        this.evaluateur = evaluateur;
    }
}
