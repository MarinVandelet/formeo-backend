package com.formeo.formeo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FeuilleEmargementLigneDto {

    public Long utilisateurId;
    public String nom;
    public String prenom;
    public String email;

    public boolean present;
    public LocalDate jourCours;
    public LocalDateTime dateHeureEmargement;

    public FeuilleEmargementLigneDto(Long utilisateurId,
                                     String nom,
                                     String prenom,
                                     String email,
                                     boolean present,
                                     LocalDate jourCours,
                                     LocalDateTime dateHeureEmargement) {
        this.utilisateurId = utilisateurId;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.present = present;
        this.jourCours = jourCours;
        this.dateHeureEmargement = dateHeureEmargement;
    }
}
