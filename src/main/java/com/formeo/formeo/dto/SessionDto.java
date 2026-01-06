package com.formeo.formeo.dto;

import java.time.LocalDateTime;

public class SessionDto {
    public Long id;
    public LocalDateTime dateDebut;
    public LocalDateTime dateFin;
    public Integer capacite;
    public String ville;
    public String adresse;
    public String salle;
    public FormationDto formation;

    public SessionDto(Long id, LocalDateTime dateDebut, LocalDateTime dateFin, Integer capacite,
                      String ville, String adresse, String salle, FormationDto formation) {
        this.id = id;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.capacite = capacite;
        this.ville = ville;
        this.adresse = adresse;
        this.salle = salle;
        this.formation = formation;
    }
}
