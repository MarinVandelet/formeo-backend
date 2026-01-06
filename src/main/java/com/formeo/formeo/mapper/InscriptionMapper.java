package com.formeo.formeo.mapper;

import com.formeo.formeo.dto.InscriptionDto;
import com.formeo.formeo.entity.Inscription;

public final class InscriptionMapper {

    private InscriptionMapper() {
    }

    public static InscriptionDto toDto(Inscription inscription) {
        if (inscription == null) {
            return null;
        }

        InscriptionDto dto = new InscriptionDto();
        dto.setId(inscription.getId());
        dto.setStatut(inscription.getStatut().name());
        dto.setCreeLe(inscription.getCreeLe());

        // On laisse DtoMapper gérer tous les sous-DTO (Session, Formation, Categorie...)
        dto.setSession(DtoMapper.toDto(inscription.getSession()));

        // Nouveaux champs pour les évaluations
        dto.setNote(inscription.getNote());
        dto.setDateEvaluation(inscription.getDateEvaluation());

        return dto;
    }
}
