package com.formeo.formeo.mapper;

import com.formeo.formeo.dto.EvaluationDto;
import com.formeo.formeo.entity.Evaluation;

public class EvaluationMapper {

    private EvaluationMapper() {}

    public static EvaluationDto toDto(Evaluation e) {
        return new EvaluationDto(
                e.getId(),
                e.getSession().getId(),
                e.getUtilisateur().getId(),
                e.getUtilisateur().getNom(),
                e.getUtilisateur().getPrenom(),
                e.getNote(),
                e.getCommentaire(),
                e.getDateEvaluation()
        );
    }
}
