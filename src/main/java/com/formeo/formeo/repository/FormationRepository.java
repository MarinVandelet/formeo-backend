package com.formeo.formeo.repository;

import com.formeo.formeo.entity.Formation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormationRepository extends JpaRepository<Formation, Long> {
    List<Formation> findByCategorieId(Long categorieId);
}
