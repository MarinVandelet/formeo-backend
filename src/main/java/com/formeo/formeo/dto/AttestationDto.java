package com.formeo.formeo.dto;

import com.formeo.formeo.entity.AttestationType;

import java.time.LocalDateTime;

public class AttestationDto {

    public Long sessionId;
    public Long utilisateurId;
    public String utilisateurNom;
    public String utilisateurPrenom;
    public String formationTitre;
    public AttestationType type;
    public Integer note;
    public LocalDateTime dateDebut;
    public LocalDateTime dateFin;
    public String ville;

    public AttestationDto(Long sessionId,
                          Long utilisateurId,
                          String utilisateurNom,
                          String utilisateurPrenom,
                          String formationTitre,
                          AttestationType type,
                          Integer note,
                          LocalDateTime dateDebut,
                          LocalDateTime dateFin,
                          String ville) {
        this.sessionId = sessionId;
        this.utilisateurId = utilisateurId;
        this.utilisateurNom = utilisateurNom;
        this.utilisateurPrenom = utilisateurPrenom;
        this.formationTitre = formationTitre;
        this.type = type;
        this.note = note;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.ville = ville;
    }
}
