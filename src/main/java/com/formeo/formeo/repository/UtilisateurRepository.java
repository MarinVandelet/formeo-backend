package com.formeo.formeo.repository;

import com.formeo.formeo.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmailIgnoreCase(String email);
    Optional<Utilisateur> findByPseudoIgnoreCase(String pseudo);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByPseudoIgnoreCase(String pseudo);
}
