package com.formeo.formeo.repository;

import com.formeo.formeo.entity.Inscription;
import com.formeo.formeo.entity.StatutInscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    long countBySessionIdAndStatutIn(Long sessionId, Iterable<StatutInscription> statuts);

    @Query("""
        select count(i) from Inscription i
        where i.utilisateur.id = :utilisateurId
          and i.statut in :statuts
          and i.session.dateDebut < :nouvelleFin
          and i.session.dateFin > :nouveauDebut
    """)
    long countInscriptionsChevauchantes(
            Long utilisateurId,
            LocalDateTime nouveauDebut,
            LocalDateTime nouvelleFin,
            List<StatutInscription> statuts
    );

    List<Inscription> findByUtilisateurId(Long utilisateurId);

    List<Inscription> findBySessionId(Long sessionId);

    List<Inscription> findBySessionIdAndStatut(Long sessionId, StatutInscription statut);

    boolean existsByUtilisateurIdAndSessionIdAndStatut(Long utilisateurId, Long sessionId, StatutInscription statut);

    long countBySessionId(Long sessionId);
}
