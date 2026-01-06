package com.formeo.formeo.repository;

import com.formeo.formeo.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {
    Optional<Categorie> findByNomIgnoreCase(String nom);
}
