package com.formeo.formeo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmargementDto {

    public Long id;
    public Long sessionId;
    public Long utilisateurId;
    public String formationTitre;
    public String ville;
    public String salle;

    public LocalDate jourCours;
    public LocalDateTime dateHeureEmargement;

    public EmargementDto(Long id,
                         Long sessionId,
                         Long utilisateurId,
                         String formationTitre,
                         String ville,
                         String salle,
                         LocalDate jourCours,
                         LocalDateTime dateHeureEmargement) {
        this.id = id;
        this.sessionId = sessionId;
        this.utilisateurId = utilisateurId;
        this.formationTitre = formationTitre;
        this.ville = ville;
        this.salle = salle;
        this.jourCours = jourCours;
        this.dateHeureEmargement = dateHeureEmargement;
    }
}
