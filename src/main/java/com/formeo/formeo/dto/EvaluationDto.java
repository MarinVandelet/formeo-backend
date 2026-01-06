package com.formeo.formeo.dto;

import java.time.LocalDateTime;

public class EvaluationDto {

    public Long id;
    public Long sessionId;
    public Long utilisateurId;
    public String utilisateurNom;
    public String utilisateurPrenom;
    public Integer note;
    public String commentaire;
    public LocalDateTime dateEvaluation;

    public EvaluationDto(Long id,
                         Long sessionId,
                         Long utilisateurId,
                         String utilisateurNom,
                         String utilisateurPrenom,
                         Integer note,
                         String commentaire,
                         LocalDateTime dateEvaluation) {
        this.id = id;
        this.sessionId = sessionId;
        this.utilisateurId = utilisateurId;
        this.utilisateurNom = utilisateurNom;
        this.utilisateurPrenom = utilisateurPrenom;
        this.note = note;
        this.commentaire = commentaire;
        this.dateEvaluation = dateEvaluation;
    }
}
