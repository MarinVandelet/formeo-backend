package com.formeo.formeo.repository;

import com.formeo.formeo.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    List<Evaluation> findBySessionId(Long sessionId);

    List<Evaluation> findByUtilisateurId(Long utilisateurId);

    Optional<Evaluation> findBySessionIdAndUtilisateurId(Long sessionId, Long utilisateurId);
}
