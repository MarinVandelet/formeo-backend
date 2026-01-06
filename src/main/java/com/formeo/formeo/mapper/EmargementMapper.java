package com.formeo.formeo.mapper;

import com.formeo.formeo.dto.EmargementDto;
import com.formeo.formeo.entity.Emargement;

public class EmargementMapper {

    private EmargementMapper() {}

    public static EmargementDto toDto(Emargement e) {
        return new EmargementDto(
                e.getId(),
                e.getSession().getId(),
                e.getUtilisateur().getId(),
                e.getSession().getFormation().getTitre(),
                e.getSession().getVille(),
                e.getSession().getSalle(),
                e.getJourCours(),
                e.getDateHeureEmargement()
        );
    }
}
