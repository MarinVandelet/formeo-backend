package com.formeo.formeo.repository;

import com.formeo.formeo.entity.Emargement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmargementRepository extends JpaRepository<Emargement, Long> {

    List<Emargement> findByUtilisateurId(Long utilisateurId);

    List<Emargement> findBySessionId(Long sessionId);

    boolean existsByUtilisateurIdAndSessionId(Long utilisateurId, Long sessionId);

    boolean existsByUtilisateurIdAndSessionIdAndJourCours(Long utilisateurId, Long sessionId, LocalDate jourCours);

    Optional<Emargement> findByUtilisateurIdAndSessionIdAndJourCours(Long utilisateurId, Long sessionId, LocalDate jourCours);
}
