package com.formeo.formeo.mapper;

import com.formeo.formeo.dto.*;
import com.formeo.formeo.entity.*;

public class DtoMapper {

    public static CategorieDto toDto(Categorie c) {
        if (c == null) return null;
        return new CategorieDto(c.getId(), c.getNom(), c.getDescription());
    }

    public static FormationDto toDto(Formation f) {
        if (f == null) return null;

        Utilisateur intervenant = f.getIntervenant();
        Long intervenantId = intervenant != null ? intervenant.getId() : null;
        String intervenantNom = intervenant != null ? intervenant.getNom() : null;
        String intervenantPrenom = intervenant != null ? intervenant.getPrenom() : null;

        return new FormationDto(
                f.getId(),
                f.getTitre(),
                f.getDescription(),
                f.getPrix(),
                f.getDureeJours(),
                toDto(f.getCategorie()),
                intervenantId,
                intervenantNom,
                intervenantPrenom
        );
    }

    public static SessionDto toDto(Session s) {
        if (s == null) return null;
        return new SessionDto(
                s.getId(),
                s.getDateDebut(),
                s.getDateFin(),
                s.getCapacite(),
                s.getVille(),
                s.getAdresse(),
                s.getSalle(),
                toDto(s.getFormation())
        );
    }
}
