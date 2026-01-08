package com.formeo.formeo.repository;

import com.formeo.formeo.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByFormationId(Long formationId);

    List<Session> findByFormationIntervenantId(Long intervenantId);
}
